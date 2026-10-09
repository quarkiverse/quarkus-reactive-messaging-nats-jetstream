package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageMetadata;

/**
 * Adds the NATS JetStream specific attributes, which the messaging semantic conventions leave to the messaging
 * system under its own {@code messaging.nats} namespace.
 */
record NatsAttributesExtractor(@NonNull Operation operation,
        boolean tracePayload) implements AttributesExtractor<Message<byte[]>, Void> {
    static final AttributeKey<String> STREAM_NAME = AttributeKey.stringKey("messaging.nats.stream.name");
    static final AttributeKey<String> CONSUMER_NAME = AttributeKey.stringKey("messaging.nats.consumer.name");
    static final AttributeKey<Long> STREAM_SEQUENCE = AttributeKey.longKey("messaging.nats.stream.sequence");
    static final AttributeKey<Long> CONSUMER_SEQUENCE = AttributeKey.longKey("messaging.nats.consumer.sequence");
    static final AttributeKey<Long> DELIVERED_COUNT = AttributeKey.longKey("messaging.nats.message.delivered_count");
    static final AttributeKey<String> PAYLOAD = AttributeKey.stringKey("messaging.nats.message.payload");

    @Override
    public void onStart(@NonNull AttributesBuilder attributes, @NonNull Context parentContext,
            @NonNull Message<byte[]> message) {
        TracedHeaders.of(operation, message)
                .flatMap(headers -> TracedHeaders.stream(message, headers))
                .ifPresent(stream -> attributes.put(STREAM_NAME, stream));
        message.getMetadata(MessageMetadata.class).ifPresent(metadata -> {
            attributes.put(CONSUMER_NAME, metadata.consumer());
            attributes.put(STREAM_SEQUENCE, metadata.streamSequence());
            attributes.put(CONSUMER_SEQUENCE, metadata.consumerSequence());
            attributes.put(DELIVERED_COUNT, metadata.deliveredCount());
        });
        // Opt-in only: the payload may contain sensitive data and can be arbitrarily large
        if (tracePayload && message.getPayload() != null) {
            attributes.put(PAYLOAD, new String(message.getPayload(), StandardCharsets.UTF_8));
        }
    }

    @Override
    public void onEnd(@NonNull AttributesBuilder attributes, @NonNull Context context, @NonNull Message<byte[]> message,
            @Nullable Void unused, @Nullable Throwable error) {
    }
}
