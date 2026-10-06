package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Waits for {@code /q/health/ready} to report that channels have verified their stream, subject and consumer
 * exist, before a test starts sending or expecting messages.
 */
public final class Readiness {

    private Readiness() {
    }

    /**
     * Waits until the application as a whole reports ready.
     */
    public static void awaitReady() {
        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(200))
                .until(() -> given().when().get("/q/health/ready").statusCode() == 200);
    }

    /**
     * Waits until the given channels report ready, for tests that deliberately configure channels which never
     * become ready.
     */
    public static void awaitReady(String... channels) {
        await().atMost(Duration.ofMinutes(1)).pollInterval(Duration.ofMillis(200))
                .until(() -> {
                    final List<Map<String, Object>> data = given().when().get("/q/health/ready").jsonPath()
                            .getList("checks.data");
                    return Arrays.stream(channels).allMatch(channel -> data.stream()
                            .anyMatch(entry -> entry != null && String.valueOf(entry.get(channel)).startsWith("[OK]")));
                });
    }
}
