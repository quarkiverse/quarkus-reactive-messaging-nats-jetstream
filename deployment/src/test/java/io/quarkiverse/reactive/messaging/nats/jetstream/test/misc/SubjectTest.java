package io.quarkiverse.reactive.messaging.nats.jetstream.test.misc;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkiverse.reactive.messaging.nats.jetstream.test.resources.*;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

public class SubjectTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(
                            TestSpanExporter.class, SubjectData.class, SubjectDataResource.class,
                            SubjectDataConsumingBean.class, MessageConsumer.class))
            .withConfigurationResource("application-subject.properties");

    @Inject
    TestSpanExporter spanExporter;

    @BeforeEach
    public void setup() {
        RestAssured.defaultParser = Parser.JSON;
        spanExporter.reset();
    }

    @Test
    public void subtopic() {
        final var messageId = "9a99811a-ef82-468e-9f0b-7879f7be16a9";
        final var data = "N6cXzM";
        final var subject = "data";
        final var subtopic = "84cee15e-f858-48d5-9b80-f5598c529ff4";

        given().pathParam("subtopic", subtopic).pathParam("id", messageId).pathParam("data", data)
                .post("/subjects/{subtopic}/{id}/{data}").then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/subjects/last").as(SubjectData.class);
            return data.equals(dataValue.data()) && data.equals(dataValue.resourceId())
                    && messageId.equals(dataValue.messageId())
                    && dataValue.subject().equals(subject + "." + subtopic);
        });
    }

}
