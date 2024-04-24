package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

public class RequestReplyTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, RequestReplyResource.class))
            .withConfigurationResource("application-request-reply.properties");

    @Inject
    TestSpanExporter spanExporter;

    @BeforeEach
    public void setup() {
        RestAssured.defaultParser = Parser.JSON;
        spanExporter.reset();
    }

    @Test
    public void requestReply() {
        final var id = "b41b2f79-118b-47c0-ba14-ae1a55ebf1e1";
        final var data = "N6cXzMdfaf";

        given()
                .pathParam("id", id).pathParam("data", data)
                .post("/request-reply/{id}/{data}")
                .then().statusCode(204);

        final var streams = given()
                .get("/request-reply/streams")
                .then().statusCode(200).extract().as(String[].class);
        assertThat(streams).contains("request-reply");

        await().atMost(30, TimeUnit.SECONDS).until(() -> {
            final var dataValue = get("/request-reply").as(Data.class);
            return data.equals(dataValue.getData()) && data.equals(dataValue.getResourceId());
        });
    }

}
