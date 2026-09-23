package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import static io.smallrye.reactive.messaging.providers.locals.ContextAwareMessage.captureContextMetadata;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;

final class MessageImpl<T> implements Message<T> {
    private org.eclipse.microprofile.reactive.messaging.Metadata metadata;
    private final T payload;

    private final Function<Metadata, CompletionStage<Void>> acknowledge;
    private final BiFunction<Throwable, org.eclipse.microprofile.reactive.messaging.Metadata, CompletionStage<Void>> notAcknowledge;

    MessageImpl(final @NonNull NativeMessage message,
            final @Nullable T payload,
            final @NonNull Context context,
            final @NonNull ConsumerConfiguration consumerConfiguration) {
        this(message, payload, context, captureContextMetadata(consumerConfiguration, MessageMetadata.of(message.metaData()),
                MessageHeaders.of(message)));
    }

    MessageImpl(final @NonNull NativeMessage message,
            final @Nullable T payload,
            final @NonNull Context context,
            final org.eclipse.microprofile.reactive.messaging.@NonNull Metadata metadata) {
        this.payload = payload;
        this.metadata = metadata;
        this.acknowledge = m -> context.runOnContext(m).apply(() -> {
            try {
                message.ack();
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        this.notAcknowledge = (throwable, m) -> context.runOnContext(m).apply(() -> {
            try {
                final var withDelay = getMetadata(m, NotAcknowledgeMetadata.class)
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
    public BiFunction<Throwable, org.eclipse.microprofile.reactive.messaging.Metadata, CompletionStage<Void>> getNackWithMetadata() {
        return notAcknowledge;
    }

    @Override
    public Function<Metadata, CompletionStage<Void>> getAckWithMetadata() {
        return acknowledge;
    }

    @SuppressWarnings({ "unchecked", "SameParameterValue" })
    private <M> Optional<M> getMetadata(Metadata metadata, Class<M> metadataClass) {
        for (Object item : metadata) {
            if (metadataClass.isInstance(item)) {
                return Optional.of((M) item);
            }
        }
        return Optional.empty();
    }
}
