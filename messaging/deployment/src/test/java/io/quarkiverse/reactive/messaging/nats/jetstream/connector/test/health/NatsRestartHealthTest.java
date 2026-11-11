package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.health;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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
 * A NATS restart is recovered by the client reconnecting, so it must stop traffic to the application (readiness)
 * without restarting it (liveness), and the application must become ready again without any traffic.
 */
public class NatsRestartHealthTest {
    private static final RestartableNatsServer nats = RestartableNatsServer.shared("restart");

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(TestSpanExporter.class, Data.class, DataResource.class, DataConsumingBean.class,
                            DataCollectorBean.class, MessageConsumer.class, RestartableNatsServer.class))
            .withConfigurationResource("application-health.properties")
            .overrideConfigKey("quarkus.messaging.nats.connection.servers", nats.url());

    @AfterAll
    static void stopNats() {
        nats.remove();
    }

    @Test
    void staysAliveAndRecoversReadinessWhenNatsRestarts() {
        await().atMost(Duration.ofSeconds(60)).until(() -> status("ready") == 200);
        assertThat(status("live")).isEqualTo(200);

        nats.stopServer();

        await().atMost(Duration.ofSeconds(30)).until(() -> {
            assertThat(status("live")).isEqualTo(200);
            return status("ready") == 503;
        });
        await().during(Duration.ofSeconds(5)).atMost(Duration.ofSeconds(10))
                .until(() -> status("live") == 200);

        nats.startServer();

        await().atMost(Duration.ofSeconds(60)).until(() -> {
            assertThat(status("live")).isEqualTo(200);
            return status("ready") == 200;
        });
    }

    private static int status(String probe) {
        return given().when().get("/q/health/" + probe).statusCode();
    }
}
