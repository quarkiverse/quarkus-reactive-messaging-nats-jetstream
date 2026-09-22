package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;
import org.jspecify.annotations.Nullable;

final class MessageImpl<T> implements Message<T> {
    private final NativeMessage message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final MessageContext context;
    private final T payload;

    MessageImpl(@NonNull NativeMessage message,
                @Nullable T payload,
                @NonNull MessageContext context,
                @NonNull ConsumerConfiguration consumerConfiguration) {
        this.message = message;
        this.payload = payload;
        this.metadata = captureContextMetadata(consumerConfiguration, MessageMetadata.of(message.metaData()),
                MessageHeaders.of(message));
        this.context = context;
    }

    MessageImpl(@NonNull NativeMessage message,
                @Nullable T payload,
                @NonNull MessageContext context,
                org.eclipse.microprofile.reactive.messaging.@NonNull Metadata metadata) {
        this.message = message;
        this.payload = payload;
        this.metadata = metadata;
        this.context = context;
    }

    @Override
    public @NonNull MessageContext context() {
        return context;
    }

    @Override
    public @NonNull NativeMessage nativeMessage() {
        return message;
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
        return context.runOnContext(this, () -> {
            try {
                message.ack();
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletionStage<Void> nack(Throwable reason, org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        return context.runOnContext(this, () -> {
            try {
                final var withDelay = getMetadata(NotAcknowledgeMetadata.class)
                        .flatMap(NotAcknowledgeMetadata::withDelay);
                if (withDelay.isPresent()) {
                    message.nakWithDelay(withDelay.get());
                } else {
                    message.nak();
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
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
        return getMetadata(LocalContextMetadata.class);
    }

    @Override
    public Message<T> addMetadata(Object metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public Message<T> withMetadata(Iterable<Object> metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public Message<T> withMetadata(
            org.eclipse.microprofile.reactive.messaging.Metadata metadata) {
        this.metadata = this.metadata.with(metadata);
        return this;
    }

    @Override
    public Message<T> withAck(Supplier<CompletionStage<Void>> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message<T> withAckWithMetadata(Function<Metadata, CompletionStage<Void>> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message<T> withNack(Function<Throwable, CompletionStage<Void>> nack) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message<T> withNackWithMetadata(BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

}
