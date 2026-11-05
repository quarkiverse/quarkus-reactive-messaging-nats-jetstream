package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.concurrency;

import static io.restassured.RestAssured.*;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.Readiness;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * An incoming channel configured with the standard {@code concurrency} attribute is split by SmallRye into one
 * {@code channel$index} copy per concurrent consumer, each of which asks the connector for its own publisher.
 */
class ConcurrencyTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Readiness.class, ConcurrencyConsumingBean.class, ConcurrencyResource.class))
            .withConfigurationResource("application-concurrency.properties");

    @BeforeEach
    void setup() {
        Readiness.awaitReady();
    }

    @Test
    void deliversToConcurrentConsumers() {
        final int values = get("/concurrency/values").as(Integer[].class).length;
        IntStream.rangeClosed(1, 9)
                .forEach(value -> given().pathParam("value", value).post("/concurrency/values/{value}").then()
                        .statusCode(204));

        await().atMost(Duration.ofSeconds(30)).until(() -> {
            final var received = Arrays.asList(get("/concurrency/values").as(Integer[].class));
            return received.size() == values + 9;
        });
    }
}
