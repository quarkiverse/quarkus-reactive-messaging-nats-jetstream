package io.quarkiverse.reactive.messaging.nats.jetstream.it;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class DataResourceTest {

    @Test
    public void data() {
        final var messageId = "8cb9fd88-08e9-422d-9f19-a3b4b3cc8cb7";
        final var data = "N6cXzM";
        final var resourceId = "56d5cc43-92dd-4df9-b385-1e412fd8fc8a";

        given()
                .header("Content-Type", "application/json")
                .pathParam("messageId", messageId)
                .body(new Data(data, resourceId))
                .post("/data/{messageId}")
                .then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/data/last").as(Data.class);
            return messageId.equals(dataValue.getMessageId()) && data.equals(dataValue.getData())
                    && resourceId.equals(dataValue.getResourceId());
        });
    }

    @Test
    public void healthLive() {
        given()
                .when().get("/q/health/live")
                .then()
                .statusCode(200);
    }

    @Test
    public void healthReady() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200);
    }
}
