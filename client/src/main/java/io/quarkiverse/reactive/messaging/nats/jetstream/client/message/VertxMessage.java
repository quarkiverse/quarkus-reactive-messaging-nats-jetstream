package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerMetadata;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.mutiny.core.Context;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

final class VertxMessage implements Message {
    private final NativeMessage message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final Context context;

    public VertxMessage(@NonNull NativeMessage message,
            @NonNull Context context, @NonNull ConsumerConfiguration consumerConfiguration) {
        this.message = message;
        this.metadata = captureContextMetadata(consumerConfiguration);
        this.context = context;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Metadata getMetadata() {
        return metadata;
    }

    @Override
    public synchronized void injectMetadata(Object metadataObject) {
        this.metadata = metadata.with(metadataObject);
    }

    @Override
    public byte[] getPayload() {
        return message.getData();
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return this::ack;
    }

    @Override
    public CompletionStage<Void> ack() {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            try {
                metadata.get(ConsumerConfiguration.class)
                        .flatMap(ConsumerConfiguration::acknowledgeTimeout)
                        .ifPresentOrElse(timeout -> {
                            try {
                                message.ackSync(timeout);
                            } catch (TimeoutException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }, message::ack);
                this.runOnMessageContext(() -> f.complete(null));
            } catch (Exception e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        return VertxContext.runOnContext(context.getDelegate(), f -> {
            try {
                final var configuration = metadata.get(ConsumerConfiguration.class)
                        .orElseThrow(IllegalStateException::new);
                final var subscribeMetadata = metadata.get(ConsumerMetadata.class)
                        .orElseThrow(IllegalStateException::new);
                getBackoff(configuration, subscribeMetadata)
                        .ifPresentOrElse(message::nakWithDelay, message::nak);
                this.runOnMessageContext(() -> f.complete(null));
            } catch (Exception e) {
                this.runOnMessageContext(() -> f.completeExceptionally(e));
            }
        });
    }

    @Override
    public Function<Throwable, CompletionStage<Void>> getNack() {
        return this::nack;
    }

    @Override
    public BiFunction<Throwable, org.eclipse.microprofile.reactive.messaging.Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return this::nack;
    }

    @Override
    public Optional<LocalContextMetadata> getContextMetadata() {
        return metadata.get(LocalContextMetadata.class);
    }

    @Override
    public Message addMetadata(Object metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public Message withMetadata(Iterable<Object> metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public Message withMetadata(
            org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    private Optional<Duration> getBackoff(@NonNull ConsumerConfiguration configuration,
            @NonNull ConsumerMetadata metadata) {
        if (configuration.backoff().isEmpty()) {
            return Optional.empty();
        } else if (metadata.deliveredCount() == 0) {
            return Optional.of(configuration.backoff().getFirst());
        } else if (metadata.deliveredCount() >= configuration.backoff().size()) {
            return Optional.of(configuration.backoff().getLast());
        } else {
            return Optional.of(configuration.backoff().get(metadata.deliveredCount() - 1));
        }
    }
}
