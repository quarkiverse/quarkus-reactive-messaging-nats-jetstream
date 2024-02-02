package io.quarkiverse.reactive.messaging.nats.jetstream.test;

import static io.restassured.RestAssured.get;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusDevModeTest;

public class ReactiveMesssagingNatsJetstreamDevModeTest {

    @RegisterExtension
    static QuarkusDevModeTest devModeTest = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(ValueConsumingBean.class, ValueProducingBean.class, ValueResource.class,
                            TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            Advisory.class, DeadLetterResource.class, DeadLetterConsumingBean.class,
                            DurableResource.class, DurableConsumingBean.class, RedeliveryResource.class,
                            RedeliveryConsumingBean.class)
                    .addAsResource("application.properties"));

    @Test
    public void testCodeUpdate() {
        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    String value = get("/value/last").asString();
                    return value.equalsIgnoreCase("20");
                });

        devModeTest.modifySourceFile(ValueProducingBean.class, s -> s.replace("* 2", "* 3"));

        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    String value = get("/value/last").asString();
                    return value.equalsIgnoreCase("30");
                });

    }
}
