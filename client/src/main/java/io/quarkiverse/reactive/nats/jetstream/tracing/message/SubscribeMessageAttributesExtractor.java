package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import static io.quarkiverse.reactive.nats.jetstream.tracing.message.MessageAttributes.*;

import java.nio.charset.StandardCharsets;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.SubscribeMetadata;

public class SubscribeMessageAttributesExtractor implements AttributesExtractor<Message, Void> {

    private static final String MESSAGE_TIMESTAMP = "message.timestamp";

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, Message message) {
        if (message.getPayload() != null) {
            attributesBuilder.put(MESSAGE_PAYLOAD, new String(message.getPayload(), StandardCharsets.UTF_8));
        }
        message.getMetadata(SubscribeMetadata.class).ifPresent(metadata -> {
            attributesBuilder.put(MESSAGE_STREAM, metadata.stream());
            attributesBuilder.put(MESSAGE_SUBJECT, metadata.subject());
            attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, metadata.streamSequence());
            attributesBuilder.put(MESSAGE_CONSUMER_SEQUENCE, metadata.consumerSequence());
            attributesBuilder.put(MESSAGE_CONSUMER, metadata.consumer());
            attributesBuilder.put(MESSAGE_DELIVERED_COUNT, metadata.deliveredCount());
            attributesBuilder.put(MESSAGE_ID, metadata.messageId());
            attributesBuilder.put(MESSAGE_TIMESTAMP, metadata.timestamp().toString());
        });
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, Message message, Void unused, Throwable error) {
    }
}
