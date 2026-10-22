package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;

record MessageImpl<T>(@Nullable T getPayload,
        @NonNull Metadata getMetadata,
        @NonNull Function<Metadata, CompletionStage<Void>> getAckWithMetadata,
        @NonNull BiFunction<Throwable, Metadata, CompletionStage<Void>> getNackWithMetadata) implements Message<T> {
    static final Function<Metadata, CompletionStage<Void>> NO_ACK = metadata -> Message.completed();
    static final BiFunction<Throwable, Metadata, CompletionStage<Void>> NO_NACK = (reason, metadata) -> Message
            .completed();

    static @NonNull Message<byte[]> received(final @NonNull NativeMessage message,
            final @NonNull Context context,
            final @NonNull ConsumerConfiguration consumerConfiguration) {
        final Function<Metadata, CompletionStage<Void>> ack = m -> context.runOnContext(m).apply(() -> {
            try {
                message.ack();
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        final BiFunction<Throwable, Metadata, CompletionStage<Void>> nack = (throwable, m) -> context.runOnContext(m)
                .apply(() -> {
                    try {
                        final var withDelay = m.get(NotAcknowledgeMetadata.class)
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
        final var metadata = ContextMetadata.capture(Metadata.of(consumerConfiguration,
                MessageMetadata.of(message.metaData()), MessageHeaders.of(message)));
        return new MessageImpl<>(message.getData(), metadata, ack, nack);
    }
}
