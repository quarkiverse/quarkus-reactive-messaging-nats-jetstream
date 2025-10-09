package io.quarkiverse.reactive.messaging.nats.jetstream.it;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.it.DataResourceCustomMappersTest.Profile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(Profile.class)
public class DataResourceCustomMappersTest {

    @Test
    public void data() {
        final var messageId = "8cb9fd88-08e9-422d-9f19-a3b4b3cc8cb7";
        final var data = "N6cXzM";
        final var resourceId = "56d5cc43-92dd-4df9-b385-1e412fd8fc8a";
        final var now = Instant.now();

        given()
                .header("Content-Type", "application/json")
                .pathParam("messageId", messageId)
                .body(new Data(data, resourceId, null, now))
                .post("/data/{messageId}")
                .then().statusCode(204);

        await().atMost(1, TimeUnit.MINUTES).until(() -> {
            final var dataValue = get("/data/last-with-timestamp").as(Data.class);
            return messageId.equals(dataValue.getMessageId()) && data.equals(dataValue.getData())
                    && resourceId.equals(dataValue.getResourceId()) && dataValue.getCreationTime().equals(now);
        });
    }

    public static class Profile implements QuarkusTestProfile {

        @ApplicationScoped
        public PayloadMapper getPayloadMapper(ObjectMapper objectMapper) {
            return new DefaultPayloadMapper(objectMapper) {
                @Override
                public <T> T of(byte[] data, Class<T> type) {
                    try {
                        return objectMapper.readerWithView(IncludeTimestamps.class).readValue(data, type);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public byte[] of(Object payload) {
                    try {
                        if (payload == null) {
                            return new byte[0];
                        } else if (payload instanceof byte[]) {
                            return (byte[]) payload;
                        } else {
                            return objectMapper.writerWithView(IncludeTimestamps.class).writeValueAsBytes(payload);
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
    }
}
