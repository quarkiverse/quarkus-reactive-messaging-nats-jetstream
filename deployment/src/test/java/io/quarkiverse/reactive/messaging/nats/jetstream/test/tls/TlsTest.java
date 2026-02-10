package io.quarkiverse.reactive.messaging.nats.jetstream.test.tls;

import static io.restassured.RestAssured.get;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.test.TestSpanExporter;
import io.quarkus.test.QuarkusDevModeTest;

public class TlsTest {

    @RegisterExtension
    static final QuarkusDevModeTest devModeTest = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(ValueConsumingBean.class, ValueProducingBean.class, ValueResource.class,
                            TestSpanExporter.class)
                    .addAsResource("application-tls.properties", "application.properties"));

    @Test
    void testTls() {
        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    String value = get("/value/last").asString();
                    return value.equalsIgnoreCase("20");
                });
    }

}
