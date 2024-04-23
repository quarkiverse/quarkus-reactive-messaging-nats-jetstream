package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

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

        final var result = given()
                .pathParam("id", id).pathParam("data", data)
                .post("/request-reply/{id}/{data}")
                .then().statusCode(200).extract().as(Data.class);

        assertThat(result).isNotNull();
        assertThat(result.getData()).isEqualTo(data);
        assertThat(result.getResourceId()).isEqualTo(id);

        final var streams = (Set<String>) get("/request-reply/streams").as(Set.class);
        assertThat(streams).contains("test");
    }

}
