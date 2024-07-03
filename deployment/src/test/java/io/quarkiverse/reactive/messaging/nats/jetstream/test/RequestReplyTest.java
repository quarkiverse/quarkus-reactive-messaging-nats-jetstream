package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;

public class RequestReplyTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, RequestReplyResource.class, StreamInfo.class))
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
        final var subject = "a65583a4-7c17-4caf-855d-de5a55aaeb6d";

        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("subject", subject)
                .pathParam("id", id)
                .pathParam("data", data)
                .post("/request-reply/subjects/{subject}/{id}/{data}")
                .then().statusCode(204);

        final var streams = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .get("/request-reply/streams")
                .then().statusCode(200).extract().as(String[].class);
        assertThat(streams).contains("request-reply");

        final var subjects = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("stream", "request-reply")
                .get("/request-reply/streams/{stream}/subjects")
                .then().statusCode(200).extract().as(String[].class);
        assertThat(subjects).contains("events.>");

        final var consumers = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("stream", "request-reply")
                .get("/request-reply/streams/{stream}/consumers")
                .then().statusCode(200).extract().as(String[].class);
        assertThat(consumers).contains(subject);

        final var result = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .pathParam("subject", subject)
                .get("/request-reply/subjects/{subject}")
                .then().statusCode(200).extract().as(Data.class);

        assertThat(result).isNotNull();
        assertThat(result.getResourceId()).isEqualTo(id);
        assertThat(result.getData()).isEqualTo(data);
    }
}
