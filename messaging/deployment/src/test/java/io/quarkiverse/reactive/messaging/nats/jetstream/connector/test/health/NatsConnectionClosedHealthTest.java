package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.health;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.MessageConsumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.TestSpanExporter;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * When the client gives up reconnecting, the connection is closed for good and only a restart of the application
 * recovers it, so liveness must fail.
 */
public class NatsConnectionClosedHealthTest {
    private static final RestartableNatsServer nats = RestartableNatsServer.shared("closed");

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class, RestartableNatsServer.class))
            .withConfigurationResource("application-health.properties")
            .overrideConfigKey("quarkus.messaging.nats.connection.servers", nats.url())
            .overrideConfigKey("quarkus.messaging.nats.connection.maximum-reconnects", "0");

    @AfterAll
    static void stopNats() {
        nats.remove();
    }

    @Test
    void isNotAliveWhenConnectionIsClosed() {
        await().atMost(Duration.ofSeconds(60)).until(() -> status("ready") == 200 && status("live") == 200);

        nats.stopServer();

        await().atMost(Duration.ofSeconds(30)).until(() -> status("live") == 503);
    }

    private static int status(String probe) {
        return given().when().get("/q/health/" + probe).statusCode();
    }
}
