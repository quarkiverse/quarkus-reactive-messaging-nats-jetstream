package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.CorrelationIdHandler;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.ReplyFailureHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.unchecked.Unchecked;

class MessageSubscriberProcessorTest {

    @Test
    void isNotHealthyUntilStreamExists() {
        final var subjects = new AtomicReference<Set<String>>(null);
        final var processor = processor(subjects, new AtomicInteger());
        final var emitter = subscribe(processor);

        try {
            emitter.emit(Message.of("published before the stream is verified"));
            assertThat(processor.health().healthy()).isFalse();

            subjects.set(Set.of("data.>"));

            waitUntil(() -> processor.health().healthy(), Duration.ofSeconds(5));
        } finally {
            processor.stop();
        }
    }

    @Test
    void isNotHealthyWhenSubjectIsNotOnStream() {
        final var lookups = new AtomicInteger();
        final var processor = processor(new AtomicReference<>(Set.of("other")), lookups);
        final var emitter = subscribe(processor);

        try {
            waitUntil(() -> lookups.get() > 1, Duration.ofSeconds(5));
            emitter.emit(Message.of("published to a subject that is not on the stream"));

            assertThat(processor.health().healthy()).isFalse();
            assertThat(processor.health().message()).contains("Subject data.test not found on stream test");
        } finally {
            processor.stop();
        }
    }

    @Test
    void stopCancelsVerification() throws InterruptedException {
        final var lookups = new AtomicInteger();
        final var processor = processor(new AtomicReference<>(null), lookups);
        subscribe(processor);

        waitUntil(() -> lookups.get() > 1, Duration.ofSeconds(5));
        processor.stop();
        final var lookupsAfterStop = lookups.get();
        Thread.sleep(200);

        assertThat(lookups.get()).isLessThanOrEqualTo(lookupsAfterStop + 1);
        assertThat(processor.health().healthy()).isFalse();
    }

    /**
     * {@code Client.publish} nacks a message it fails to publish and completes normally, so its Uni only fails when
     * the nack itself fails. When that happens the channel must keep publishing subsequent messages.
     */
    @Test
    void publishesMessagesAfterPublishFailure() {
        final var published = new CopyOnWriteArrayList<String>();
        final var processor = processor(new AtomicReference<>(Set.of("data.>")), new AtomicInteger(),
                message -> {
                    if (message.getPayload().equals("fails")) {
                        return Uni.createFrom().failure(new IllegalStateException("publish failed"));
                    }
                    published.add(message.getPayload());
                    return Uni.createFrom().item(message);
                });
        final var emitter = subscribe(processor);

        try {
            emitter.emit(Message.of("fails"));
            waitUntil(() -> !processor.health().message().contains("ready"), Duration.ofSeconds(5));
            emitter.emit(Message.of("after failure"));

            waitUntil(() -> published.contains("after failure"), Duration.ofSeconds(5));
        } finally {
            processor.stop();
        }
    }

    private MultiEmitter<? super Message<String>> subscribe(MessageSubscriberProcessor<String> processor) {
        final var emitter = new AtomicReference<MultiEmitter<? super Message<String>>>();
        Multi.createFrom().<Message<String>> emitter(emitter::set).subscribe(processor.subscriber());
        return emitter.get();
    }

    /**
     * @param subjects the subjects of the stream, or {@code null} while the stream does not exist
     * @param lookups counts the stream lookups made by the processor
     */
    private MessageSubscriberProcessor<String> processor(AtomicReference<Set<String>> subjects, AtomicInteger lookups) {
        return processor(subjects, lookups, message -> Uni.createFrom().item(message));
    }

    @SuppressWarnings("unchecked")
    private MessageSubscriberProcessor<String> processor(AtomicReference<Set<String>> subjects, AtomicInteger lookups,
            Function<Message<String>, Uni<Message<String>>> publish) {
        final var streamManagement = (StreamManagement) Proxy.newProxyInstance(StreamManagement.class.getClassLoader(),
                new Class<?>[] { StreamManagement.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("stream")) {
                        return Uni.createFrom().item(Unchecked.supplier(() -> {
                            lookups.incrementAndGet();
                            final var current = subjects.get();
                            if (current == null) {
                                throw new IllegalStateException("stream not found");
                            }
                            return stream(current);
                        }));
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        final var client = (Client) Proxy.newProxyInstance(Client.class.getClassLoader(), new Class<?>[] { Client.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("streamManagement")) {
                        return streamManagement;
                    }
                    if (method.getName().equals("publish")) {
                        return publish.apply((Message<String>) args[0]);
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        return new MessageSubscriberProcessor<>(configuration(), client);
    }

    private static Stream stream(Set<String> subjects) {
        final var configuration = (StreamConfiguration) Proxy.newProxyInstance(
                StreamConfiguration.class.getClassLoader(), new Class<?>[] { StreamConfiguration.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("subjects")) {
                        return subjects;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        return (Stream) Proxy.newProxyInstance(Stream.class.getClassLoader(), new Class<?>[] { Stream.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("configuration")) {
                        return configuration;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
    }

    private static PublisherChannelConfiguration configuration() {
        return new PublisherChannelConfiguration() {
            @Override
            public @NonNull String name() {
                return "test-out";
            }

            @Override
            public @NonNull String stream() {
                return "test";
            }

            @Override
            public @NonNull Optional<Duration> retryBackoff() {
                return Optional.of(Duration.ofMillis(10));
            }

            @Override
            public @NonNull Optional<String> datasource() {
                return Optional.empty();
            }

            @Override
            public @NonNull String subject() {
                return "data.test";
            }

            @Override
            public @NonNull Optional<String> replySubject() {
                return Optional.empty();
            }

            @Override
            public @NonNull Optional<Duration> replyTimeout() {
                return Optional.empty();
            }

            @Override
            public @NonNull Optional<Duration> replyInactiveThreshold() {
                return Optional.empty();
            }

            @SuppressWarnings({ "NullableProblems", "DataFlowIssue" })
            @Override
            public CorrelationIdHandler replyCorrelationIdHandler() {
                return null;
            }

            @Override
            public @NonNull Optional<ReplyFailureHandler> replyFailureHandler() {
                return Optional.empty();
            }
        };
    }

    private static void waitUntil(BooleanSupplier condition, Duration timeout) {
        final var deadline = System.nanoTime() + timeout.toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Condition not met within " + timeout);
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError(e);
            }
        }
    }
}
