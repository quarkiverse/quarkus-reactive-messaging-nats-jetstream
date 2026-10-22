package io.quarkiverse.reactive.messaging.nats.client.it;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ClientResourceTest {

    @Test
    void publishAndNext() {
        final var data = new Data("N6cXzM", "56d5cc43-92dd-4df9-b385-1e412fd8fc8a");

        final var sequence = given()
                .header("Content-Type", "application/json")
                .body(data)
                .post("/data")
                .then().statusCode(200)
                .extract().asString();
        assertThat(Long.parseLong(sequence)).isGreaterThan(0);

        final var received = given()
                .get("/data/next")
                .then().statusCode(200)
                .extract().as(Data.class);
        assertThat(data).isEqualTo(received);
    }

    @Test
    void keyValue() {
        final var key = "f3b0c2a4";

        given()
                .header("Content-Type", "text/plain")
                .body("value")
                .put("/key-value/{key}", key)
                .then().statusCode(200);

        given()
                .get("/key-value/{key}", key)
                .then().statusCode(200)
                .body(equalTo("value"));

        given()
                .delete("/key-value/{key}", key)
                .then().statusCode(204);

        given()
                .get("/key-value/{key}", key)
                .then().statusCode(404);
    }
}
