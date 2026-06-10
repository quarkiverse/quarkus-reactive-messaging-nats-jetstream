package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.PublishMetadata;

import java.nio.charset.StandardCharsets;

import static io.quarkiverse.reactive.nats.jetstream.tracing.message.MessageAttributes.*;

public class PublishMessageAttributesExtractor implements AttributesExtractor<Message, Void> {

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, Message message) {
        attributes.put(MESSAGE_PAYLOAD, new String(message.getPayload(), StandardCharsets.UTF_8));
        message.getMetadata(PublishMetadata.class).ifPresent(metadata -> {
            attributes.put(MESSAGE_TYPE, metadata.getPayloadType().toString());
            attributes.put(MESSAGE_STREAM, metadata.stream());
            attributes.put(MESSAGE_SUBJECT, metadata.subject());
            attributes.put(MESSAGE_ID, metadata.messageId());
        });
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, Message message, Void unused,
            Throwable error) {
    }
}
