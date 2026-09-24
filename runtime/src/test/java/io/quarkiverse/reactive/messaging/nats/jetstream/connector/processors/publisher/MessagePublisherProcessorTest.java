package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.subscription.MultiEmitter;

class MessagePublisherProcessorTest {

    @Test
    void retriesSubscriptionFailuresWhileRunning() {
        final var attempts = new AtomicInteger();
        final var message = Message.of("recovered");
        final Multi<Message<String>> source = Multi.createFrom().deferred(() -> {
            if (attempts.incrementAndGet() <= 2) {
                return Multi.createFrom().failure(new IOException("Subscription timed out"));
            }
            return Multi.createFrom().item(message);
        });
        final var processor = processor(() -> source);
        final var subscriber = processor.publisher().subscribe().withSubscriber(AssertSubscriber.create(1));

        try {
            subscriber.awaitCompletion(Duration.ofSeconds(5)).assertCompleted().assertItems(message);
            assertThat(attempts.get()).isEqualTo(3);
            assertThat(processor.health().healthy()).isTrue();
        } finally {
            subscriber.cancel();
        }
    }

    @Test
    void recreatesClientSubscriptionAfterFailure() {
        final var attempts = new AtomicInteger();
        final var message = Message.of("recovered");
        final var processor = processor(() -> {
            final var executor = Executors.newSingleThreadExecutor();
            final Multi<Message<String>> source = attempts.incrementAndGet() == 1
                    ? Multi.createFrom().failure(new IOException("Subscription timed out"))
                    : Multi.createFrom().item(message);
            return source.runSubscriptionOn(executor).onTermination().invoke(executor::shutdown);
        });
        final var subscriber = processor.publisher().subscribe().withSubscriber(AssertSubscriber.create(1));

        try {
            subscriber.awaitCompletion(Duration.ofSeconds(5)).assertCompleted().assertItems(message);
            assertThat(attempts.get()).isEqualTo(2);
            assertThat(processor.health().healthy()).isTrue();
        } finally {
            subscriber.cancel();
        }
    }

    @Test
    void doesNotRetrySubscriptionFailuresAfterStop() {
        final var attempts = new AtomicInteger();
        final var emitter = new AtomicReference<MultiEmitter<? super Message<String>>>();
        final Multi<Message<String>> source = Multi.createFrom().deferred(() -> {
            attempts.incrementAndGet();
            return Multi.createFrom().<Message<String>> emitter(emitter::set);
        });
        final var processor = processor(() -> source);
        final var subscriber = processor.publisher().subscribe().withSubscriber(AssertSubscriber.create(1));

        try {
            assertThat(processor.health().healthy()).isTrue();
            processor.stop();
            emitter.get().fail(new IOException("Disconnected"));

            subscriber.awaitFailure(Duration.ofSeconds(5)).assertFailedWith(IOException.class, "Disconnected");
            assertThat(attempts.get()).isEqualTo(1);
            assertThat(processor.health().healthy()).isFalse();
        } finally {
            subscriber.cancel();
        }
    }

    private MessagePublisherProcessor<String> processor(Supplier<Multi<Message<String>>> source) {
        final var configuration = new ConsumerChannelConfigurationImpl("test-in", "test",
                Optional.of(Duration.ofMillis(10)), Optional.empty(), Optional.of("test-consumer"), Optional.empty(),
                1, Duration.ofSeconds(1));
        final var client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[] { Client.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("subscribe")) {
                        return source.get();
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        return new MessagePublisherProcessor<>(configuration, client);
    }
}
