package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.continuous;

import static io.restassured.RestAssured.*;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Arrays;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.Readiness;
import io.quarkus.test.QuarkusExtensionTest;

class ContinuousPullTest {

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Readiness.class, ContinuousConsumingBean.class, ContinuousResource.class))
            .withConfigurationResource("application-continuous.properties");

    @BeforeEach
    void setup() {
        Readiness.awaitReady();
    }

    @Test
    void deliversPartialBatch() {
        final int values = get("/continuous/values").as(Integer[].class).length;
        publish("/continuous/values/{value}");

        // the channel pulls up to 100 messages and waits 15 seconds for them, so the pull window only ends long after
        // the published messages are available: they must be delivered as they arrive, not when the window ends
        await().atMost(Duration.ofSeconds(5)).until(() -> {
            final var received = get("/continuous/values").as(Integer[].class);
            return received.length == values + 3;
        });
    }

    @Test
    void deliversPartialTypedBatch() {
        final int values = get("/continuous/typed-values").as(Integer[].class).length;
        publish("/continuous/typed-values/{value}");

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            final var received = get("/continuous/typed-values").as(Integer[].class);
            return received.length == values + 3;
        });
    }

    @Test
    void keepsPartialBatchOrder() {
        final int values = get("/continuous/values").as(Integer[].class).length;
        final int typedValues = get("/continuous/typed-values").as(Integer[].class).length;
        publish("/continuous/values/{value}");
        publish("/continuous/typed-values/{value}");

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            final var received = Arrays.asList(get("/continuous/values").as(Integer[].class));
            return received.size() == values + 3
                    && received.subList(values, received.size()).equals(Arrays.asList(1, 2, 3));
        });

        await().atMost(Duration.ofSeconds(5)).until(() -> {
            final var received = Arrays.asList(get("/continuous/typed-values").as(Integer[].class));
            return received.size() == typedValues + 3
                    && received.subList(typedValues, received.size()).equals(Arrays.asList(1, 2, 3));
        });
    }

    private static void publish(String path) {
        for (int value = 1; value <= 3; value++) {
            given().pathParam("value", value).post(path).then().statusCode(204);
        }
    }
}
