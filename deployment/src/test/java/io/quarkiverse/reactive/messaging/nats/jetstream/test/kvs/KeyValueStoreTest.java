package io.quarkiverse.reactive.messaging.nats.jetstream.test.kvs;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.parsing.Parser;

public class KeyValueStoreTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, KeyValueStoreResource.class))
            .withConfigurationResource("application-key-value.properties");

    @Inject
    TestSpanExporter spanExporter;

    @BeforeEach
    public void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void putValue() {
        final var data = new Data("test data", "4b9d58c8-1f15-4b1e-8606-55c5f72861c8", "316ac292-e06d-4ed6-9aa6-5a25f996a76a");
        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Content-Type", "application/json")
                .pathParam("key", "test-key")
                .body(data)
                .put("/key-value/{key}")
                .then().statusCode(200);
    }

    @Test
    public void getValue() {
        final var data = new Data("test data 2", "a94903e3-452f-4aa2-8ceb-243acfc3114a",
                "aa5ea107-452f-4750-9882-360b5a211905");
        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Content-Type", "application/json")
                .pathParam("key", "test-key-2")
                .body(data)
                .put("/key-value/{key}")
                .then().statusCode(200);

        final var value = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Accept", "application/json")
                .pathParam("key", "test-key-2")
                .get("/key-value/{key}")
                .then().statusCode(200).extract().as(Data.class);

        assertThat(value).isEqualTo(data);
    }

    @Test
    public void deleteValue() {
        final var data = new Data("test data 3", "d2405c89-fd15-40e0-aa29-263b6935e2fc",
                "b62e5b17-b6d7-4149-bf00-525bf833473d");
        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Content-Type", "application/json")
                .pathParam("key", "test-key-3")
                .body(data)
                .put("/key-value/{key}")
                .then().statusCode(200);

        final var value = given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Accept", "application/json")
                .pathParam("key", "test-key-3")
                .get("/key-value/{key}")
                .then().statusCode(200).extract().as(Data.class);

        assertThat(value).isEqualTo(data);

        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Accept", "application/json")
                .pathParam("key", "test-key-3")
                .delete("/key-value/{key}")
                .then().statusCode(204);

        given()
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .header("Accept", "application/json")
                .pathParam("key", "test-key-3")
                .get("/key-value/{key}")
                .then().statusCode(204);
    }

}
