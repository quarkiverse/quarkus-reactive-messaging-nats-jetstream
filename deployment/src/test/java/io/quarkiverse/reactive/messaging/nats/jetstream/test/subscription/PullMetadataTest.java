package io.quarkiverse.reactive.messaging.nats.jetstream.test.subscription;

import static io.restassured.RestAssured.*;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.parsing.Parser;

class PullMetadataTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class))
            .withConfigurationResource("application-pull-metadata.properties");

    @BeforeEach
    void setup() {
        defaultParser = Parser.JSON;
    }

    @Test
    void metadata() {
        final var messageId = "4dc58197-8cfb-4099-a211-25d5c2d04f4b";
        final var data = "N6cXzM";

        given().pathParam("id", messageId).pathParam("data", data).post("/data/{id}/{data}").then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/data/last").as(Data.class);
            return data.equals(dataValue.data()) && data.equals(dataValue.resourceId())
                    && messageId.equals(dataValue.messageId());
        });
    }
}
