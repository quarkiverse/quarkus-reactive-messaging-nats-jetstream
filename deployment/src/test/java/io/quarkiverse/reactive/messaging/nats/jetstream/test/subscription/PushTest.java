package io.quarkiverse.reactive.messaging.nats.jetstream.test.subscription;

import static io.restassured.RestAssured.*;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.Advisory;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;

class PushTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(ValueConsumingBean.class, ValueProducingBean.class, ValueResource.class,
                            TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            Advisory.class, DeadLetterResource.class, DeadLetterConsumingBean.class,
                            DurableResource.class, DurableConsumingBean.class, RedeliveryResource.class,
                            RedeliveryConsumingBean.class, DataCollectorBean.class, MessageConsumer.class))
            .withConfigurationResource("application-push.properties");

    @BeforeEach
    void setup() {
        defaultParser = Parser.JSON;
    }

    @Test
    void readiness() {
        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when().get("/q/health/ready")
                .then()
                .statusCode(200);
    }

    @Test
    void liveness() {
        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .when().get("/q/health/live")
                .then()
                .statusCode(200);
    }

    @Test
    void metadata() {
        final var messageId = "4e54818a-c624-495a-81c8-0145ad4c9925";
        final var data = "N6cX533zM";

        given().pathParam("id", messageId).pathParam("data", data).post("/data/{id}/{data}").then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/data/last").as(Data.class);
            return data.equals(dataValue.data()) && data.equals(dataValue.resourceId())
                    && messageId.equals(dataValue.messageId());
        });
    }

    @Test
    void deadLetter() {
        final var messageId = "342646ee-acc5-4acd-b35d-a222568a127f";
        final var data = "6UFqFISmfk";

        given().header("Content-Type", "application/json").body(new Data(data, messageId, messageId))
                .post("/dead-letter/data").then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/dead-letter/last").as(Data.class);
            return data.equals(dataValue.data()) && messageId.equals(dataValue.resourceId())
                    && messageId.equals(dataValue.messageId());
        });
    }

    @Test
    void durableConsumer() {
        for (int i = 1; i <= 5; i++) {
            given().pathParam("data", i).post("/durable/{data}").then().statusCode(204);
        }
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var values = Arrays.asList(get("/durable/values").as(Integer[].class));
            return values.size() == 5 && values.contains(1) && values.contains(2) && values.contains(3) && values.contains(4)
                    && values.contains(5);
        });
    }

    @Test
    void redelivery() {
        given().pathParam("data", 42).post("/redelivery/{data}").then().statusCode(204);
        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var value = get("/redelivery/last").asString();
            return value.equalsIgnoreCase("42");
        });
    }
}
