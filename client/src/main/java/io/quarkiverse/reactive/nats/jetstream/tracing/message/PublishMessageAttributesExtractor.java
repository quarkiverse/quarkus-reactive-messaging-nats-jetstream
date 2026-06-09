package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.PublishMetadata;

import java.nio.charset.StandardCharsets;

public class PublishMessageAttributesExtractor implements AttributesExtractor<Message, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_TYPE = "message.type";

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, Message message) {
        attributes.put(MESSAGE_PAYLOAD, new String(message.getPayload(), StandardCharsets.UTF_8));
        message.getMetadata(PublishMetadata.class).map(metadata -> attributes.put(MESSAGE_TYPE, metadata.getPayloadType().toString()))
                .orElseGet(() -> attributes.put(MESSAGE_TYPE, byte[].class.toString()));
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, Message message, Void unused,
            Throwable error) {
    }
}
