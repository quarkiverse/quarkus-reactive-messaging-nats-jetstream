package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;
import io.smallrye.reactive.messaging.providers.helpers.VertxContext;
import io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import io.vertx.core.Context;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

final class VertxMessage implements Message<T>, ContextAwareMessage<T>, MetadataInjectableMessage<T> {
    private final NativeMessage message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final Context context;

    public VertxMessage(@NonNull NativeMessage message,
                        @NonNull Context context) {
        this.message = message;
        this.metadata = captureContextMetadata();
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

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Void> ack() {
        return VertxContext.runOnContext(context, f -> {
            try {
                final var configuration = metadata.get(MessageConfiguration.class)
                        .map(c -> (MessageConfiguration<T>) c);
                configuration.flatMap(MessageConfiguration::acknowledgeTimeout)
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

    @SuppressWarnings("unchecked")
    @Override
    public CompletionStage<Void> nack(Throwable reason, org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        return VertxContext.runOnContext(context, f -> {
            try {
                final var configuration = metadata.get(MessageConfiguration.class)
                        .map(c -> (MessageConfiguration<T>) c)
                        .orElseThrow(IllegalStateException::new);
                final var subscribeMetadata = metadata.get(SubscribeMetadata.class)
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

    private Optional<Duration> getBackoff(@NonNull MessageConfiguration<T> messageConfiguration, @NonNull SubscribeMetadata metadata) {
        if (messageConfiguration.backoff().isEmpty()) {
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
