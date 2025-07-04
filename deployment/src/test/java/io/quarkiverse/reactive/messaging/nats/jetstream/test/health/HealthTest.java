package io.quarkiverse.reactive.messaging.nats.jetstream.test.health;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import org.eclipse.microprofile.reactive.messaging.*;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;

public class HealthTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class))
            .withConfigurationResource("application-health.properties");

    @Test
    public void readiness() {
        await().atMost(60, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            try {
                given()
                        .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                        .when().get("/q/health/ready")
                        .then()
                        .statusCode(200);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
    }

    @Test
    public void liveness() {
        await().atMost(60, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(() -> {
            try {
                given()
                        .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                        .when().get("/q/health/live")
                        .then()
                        .statusCode(200);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        });
    }

    @Path("/data")
    @Produces("application/json")
    public static class DataResource {

        @Inject
        DataCollectorBean bean;

        @Channel("data")
        Emitter<String> emitter;

        @GET
        @Path("/last")
        public io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data getLast() {
            return bean.getLast().orElseGet(
                    () -> new io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data(null, null, null));
        }

        @POST
        @Path("/{id}/{data}")
        public Uni<Void> produceData(@PathParam("id") String id, @PathParam("data") String data) {
            return emitData(id, data).onItem().transformToUni(m -> Uni.createFrom().voidItem());
        }

        private Uni<Message<String>> emitData(String id, String data) {
            return Uni.createFrom().item(() -> {
                final var headers = new HashMap<String, List<String>>();
                headers.put("RESOURCE_ID", List.of(data));
                final var message = Message.of(data, Metadata.of(PublishMessageMetadata.of(id, headers)));
                emitter.send(message);
                return message;
            });
        }
    }

    @ApplicationScoped
    static class DataConsumingBean implements MessageConsumer<String> {
        private final static Logger logger = Logger
                .getLogger(io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.DataConsumingBean.class);

        private final Emitter<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> dataEmitter;

        public DataConsumingBean(
                @Channel("data-emitter") Emitter<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> dataEmitter) {
            this.dataEmitter = dataEmitter;
        }

        @Incoming("data-consumer")
        public Uni<Void> data(Message<String> message) {
            return Uni.createFrom().item(message)
                    .onItem().invoke(m -> logger.infof("Received message: %s", message))
                    .onItem().transformToUni(this::publish)
                    .onItem().transformToUni(this::acknowledge)
                    .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
        }

        private Uni<Message<String>> publish(Message<String> message) {
            try {
                return Uni.createFrom()
                        .item(() -> message.getMetadata(SubscribeMessageMetadata.class)
                                .map(metadata -> Tuple2.of(metadata.headers().get("RESOURCE_ID").get(0), metadata.messageId()))
                                .orElse(Tuple2.of(null, null)))
                        .onItem()
                        .transformToUni(tuple -> Uni.createFrom()
                                .completionStage(
                                        dataEmitter
                                                .send(new io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data(
                                                        message.getPayload(), tuple.getItem1(), tuple.getItem2()))))
                        .onItem().transform(ignore -> message);
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        }
    }

    @ApplicationScoped
    static class DataCollectorBean implements
            io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> {
        private final static Logger logger = Logger
                .getLogger(io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.DataCollectorBean.class);

        private final AtomicReference<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> lastData = new AtomicReference<>();

        @Incoming("data-collector")
        public Uni<Void> data(Message<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> message) {
            return Uni.createFrom().item(message)
                    .onItem().invoke(m -> logger.infof("Received message: %s", message))
                    .onItem().transformToUni(this::setLast)
                    .onItem().transformToUni(this::acknowledge)
                    .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
        }

        private Uni<Message<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data>> setLast(
                Message<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> message) {
            return Uni.createFrom().item(() -> {
                lastData.set(message.getPayload());
                return message;
            });
        }

        public Optional<io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.Data> getLast() {
            return Optional.ofNullable(lastData.get());
        }

    }

    record Data(String data, String resourceId, String messageId) {
    }

}
