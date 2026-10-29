package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static org.assertj.core.api.Assertions.fail;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkus.test.QuarkusExtensionTest;

/**
 * A push subscription runs its dispatcher on the connection executor (the Quarkus worker pool), so cancelling it must
 * release the dispatcher; otherwise every cancelled subscription keeps a worker thread, and once the pool is
 * exhausted new subscriptions silently receive nothing.
 */
public class PushSubscriptionTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final int MAX_THREADS = 10;

    @RegisterExtension
    static final QuarkusExtensionTest config = new QuarkusExtensionTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClasses(Data.class, ClientConsumerConfiguration.class, PushConsumerConfiguration.class))
            .withConfigurationResource("application-client.properties")
            .overrideConfigKey("quarkus.thread-pool.max-threads", String.valueOf(MAX_THREADS))
            .overrideConfigKey(
                    "quarkus.log.category.\"io.quarkiverse.reactive.messaging.nats.jetstream.client.ConsumerImpl\".level",
                    "DEBUG");

    @Inject
    Client client;

    @Test
    void cancelledSubscriptionsReleaseTheirDispatcher() throws InterruptedException {
        for (int i = 0; i < 3 * MAX_THREADS; i++) {
            final var consumer = "push-subscription-" + UUID.randomUUID();
            client.consumerManagement("client-test")
                    .addIfAbsent(new PushConsumerConfiguration(consumer, "client-test.push", "_push." + consumer))
                    .await().atMost(TIMEOUT);
            final var received = new CountDownLatch(1);
            final var failure = new AtomicReference<Throwable>();
            final var subscription = client.<String> subscribe("client-test", consumer)
                    .subscribe().with(message -> {
                        received.countDown();
                        message.ack();
                    }, failure::set);
            client.publish(Message.of("message-" + i), "client-test", "client-test.push").await().atMost(TIMEOUT);

            if (!received.await(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                // Diagnostics for intermittent failures on CI: whether the server delivered the message to the client
                final var info = client.consumerManagement("client-test").consumer(consumer).await().atMost(TIMEOUT);
                fail("Message %d not received by consumer %s: subscription failure=%s, delivered=%s, pending=%d, "
                        + "acknowledge pending=%d, push bound=%s, running dispatchers=%d (max threads %d)", i, consumer,
                        failure.get(), info.delivered(), info.pending(), info.acknowledgePending(), info.pushBound(),
                        runningDispatchers(), MAX_THREADS);
            }
            subscription.cancel();
            client.consumerManagement("client-test").delete(consumer).await().atMost(TIMEOUT);
        }
    }

    private static long runningDispatchers() {
        return Thread.getAllStackTraces().values().stream()
                .filter(stack -> Arrays.stream(stack)
                        .anyMatch(frame -> frame.getClassName().equals("io.nats.client.impl.NatsDispatcher")
                                && frame.getMethodName().equals("run")))
                .count();
    }
}
