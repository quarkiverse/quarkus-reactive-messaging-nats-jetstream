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

final class MessageImpl implements Message {
    private final NativeMessage message;
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final MessageContext context;

    MessageImpl(@NonNull NativeMessage message,
            @NonNull MessageContext context,
            @NonNull ConsumerConfiguration consumerConfiguration) {
        this.message = message;
        this.metadata = captureContextMetadata(consumerConfiguration, MessageMetadata.of(message.metaData()),
                MessageHeaders.of(message));
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
        return context.runOnContext(this, () -> {
            try {
                final var configuration = getMetadata(ConsumerConfiguration.class)
                        .orElseThrow(() -> new IllegalStateException("Consumer configuration not found"));
                final var timeout = configuration.acknowledgeTimeout();
                message.ackSync(timeout);
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
                final var withDelay = getMetadata(metadata, NotAcknowledgeMetadata.class)
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

    @Override
    public Message withAck(Supplier<CompletionStage<Void>> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message withAckWithMetadata(Function<Metadata, CompletionStage<Void>> supplier) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message withNack(Function<Throwable, CompletionStage<Void>> nack) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Message withNackWithMetadata(BiFunction<Throwable, Metadata, CompletionStage<Void>> nack) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @SuppressWarnings("unchecked")
    private <M> Optional<M> getMetadata(org.eclipse.microprofile.reactive.messaging.Metadata metadata,
            Class<? extends M> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("`clazz` must not be `null`");
        }
        for (Object item : metadata) {
            if (clazz.isInstance(item)) {
                return Optional.of((M) item);
            }
        }
        return Optional.empty();
    }

}
