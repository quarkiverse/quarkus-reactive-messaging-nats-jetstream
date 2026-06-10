package io.quarkiverse.reactive.nats.jetstream.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.core.Context;

final class VertxMessage<T> implements Message<T>, ContextAwareMessage<T>, MetadataInjectableMessage<T> {
    private final NativeMessage message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final Context context;
    private final T payload;

    public VertxMessage(@NonNull NativeMessage message,
                        @NonNull Context context,
                        @NonNull T payload) {
        this.message = message;
        this.metadata = captureContextMetadata();
        this.context = context;
        this.payload = payload;
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
    public T getPayload() {
        return payload;
    }

    @Override
    public Supplier<CompletionStage<Void>> getAck() {
        return this::ack;
    }

    @Override
    public CompletionStage<Void> ack() {
        return VertxContext.runOnContext(context, f -> {
            try {
                final var messageMetadata = metadata.get(MessageConfiguration.class);
                messageMetadata.flatMap(MessageConfiguration::acknowledgeTimeout)
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
        return VertxContext.runOnContext(context, f -> {
            try {
                getBackoff(metadata.get(MessageConfiguration.class).orElse(null), metadata.get(SubscribeMetadata.class).orElse(null))
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
    public org.eclipse.microprofile.reactive.messaging.Message<T> addMetadata(Object metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<T> withMetadata(Iterable<Object> metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public org.eclipse.microprofile.reactive.messaging.Message<T> withMetadata(
            org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    private Optional<Duration> getBackoff(@Nullable MessageConfiguration messageConfiguration, @Nullable SubscribeMetadata metadata) {
        if (messageConfiguration == null || metadata == null) {
            return Optional.empty();
        } else if (messageConfiguration.backoff().isEmpty()) {
            return Optional.empty();
        } else if (metadata.deliveredCount() == 0) {
            return Optional.of(messageConfiguration.backoff().getFirst());
        } else if (metadata.deliveredCount() >= messageConfiguration.backoff().size()) {
            return Optional.of(messageConfiguration.backoff().getLast());
        } else {
            return Optional.of(messageConfiguration.backoff().get(metadata.deliveredCount() - 1));
        }
    }
}
