package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.SubscribeMetadata;

import java.nio.charset.StandardCharsets;

public class SubscribeMessageAttributesExtractor implements AttributesExtractor<SubscribeMetadata, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    private static final String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    private static final String MESSAGE_CONSUMER = "message.consumer";
    private static final String MESSAGE_DELIVERED_COUNT = "message.delivered_count";

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, SubscribeMetadata metadata) {
        attributesBuilder.put(MESSAGE_PAYLOAD, new String(metadata.payload(), StandardCharsets.UTF_8));
        attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, metadata.streamSequence());
        attributesBuilder.put(MESSAGE_CONSUMER_SEQUENCE, metadata.consumerSequence());
        attributesBuilder.put(MESSAGE_CONSUMER, metadata.consumer());
        attributesBuilder.put(MESSAGE_DELIVERED_COUNT, metadata.deliveredCount());
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, SubscribeMetadata metadata, Void unused, Throwable error) {
    }
}
