package io.quarkiverse.reactive.messaging.nats.jetstream.test.dev;

import static io.restassured.RestAssured.get;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusDevModeTest;

public class DevModeTest {

    @RegisterExtension
    static final QuarkusDevModeTest devModeTest = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(ValueConsumingBean.class, ValueProducingBean.class, ValueResource.class,
                            TestSpanExporter.class)
                    .addAsResource("application-dev.properties", "application.properties"));

    @Test
    void testCodeUpdate() {
        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    String value = get("/value/last").asString();
                    return value.equalsIgnoreCase("20");
                });

        devModeTest.modifySourceFile(ValueProducingBean.class, s -> s.replace("* 2", "* 3"));

        await()
                .atMost(3, TimeUnit.MINUTES)
                .until(() -> {
                    String value = get("/value/last").asString();
                    return value.equalsIgnoreCase("30");
                });

    }
}
