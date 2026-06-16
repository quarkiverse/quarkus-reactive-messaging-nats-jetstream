package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import java.nio.charset.StandardCharsets;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.internal.SpanKey;
import io.opentelemetry.instrumentation.api.internal.SpanKeyProvider;
import io.quarkiverse.reactive.messaging.nats.jetstream.consumer.ConsumerMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.*;

class MessageAttributesExtractor implements AttributesExtractor<Message, Void>, SpanKeyProvider {
    private final static AttributeKey<String> MESSAGING_DESTINATION_NAME = AttributeKey.stringKey("messaging.destination.name");
    private final static AttributeKey<String> MESSAGING_MESSAGE_ID = AttributeKey.stringKey("messaging.message.id");
    private final static AttributeKey<String> MESSAGING_OPERATION = AttributeKey.stringKey("messaging.operation");
    private final static AttributeKey<String> MESSAGING_SYSTEM = AttributeKey.stringKey("messaging.system");
    private final static AttributeKey<String> MESSAGING_STREAM = AttributeKey.stringKey("messaging.stream.name");
    private final static AttributeKey<String> MESSAGING_SUBJECT = AttributeKey.stringKey("messaging.subject");
    private final static AttributeKey<String> MESSAGING_MESSAGE_PAYLOAD = AttributeKey.stringKey("messaging.message.payload");
    private final static AttributeKey<Long> MESSAGING_STREAM_SEQUENCE = AttributeKey.longKey("messaging.stream.sequence");
    private final static AttributeKey<Long> MESSAGING_CONSUMER_SEQUENCE = AttributeKey.longKey("messaging.consumer.sequence");
    private final static AttributeKey<String> MESSAGING_CONSUMER = AttributeKey.stringKey("messaging.consumer.name");
    private final static AttributeKey<Long> MESSAGING_MESSAGE_DELIVERED_COUNT = AttributeKey
            .longKey("messaging.message.delivered.count");
    private final static AttributeKey<String> MESSAGING_MESSAGE_TYPE = AttributeKey.stringKey("messaging.message.type");
    private final static AttributeKey<String> MESSAGING_MESSAGE_TIMESTAMP = AttributeKey
            .stringKey("messaging.message.timestamp");

    private final Operation operation;

    MessageAttributesExtractor(Operation operation) {
        this.operation = operation;
    }

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, Message message) {
        attributes.put(MESSAGING_SYSTEM, "jetstream");
        attributes.put(MESSAGING_DESTINATION_NAME, getDestination(message));
        attributes.put(MESSAGING_OPERATION, operation.toString());
        attributes.put(MESSAGING_MESSAGE_PAYLOAD, new String(message.getPayload(), StandardCharsets.UTF_8));
        message.getMetadata(Headers.class).ifPresent(metadata -> {
            attributes.put(MESSAGING_MESSAGE_TYPE, metadata.payloadType().map(Class::toString).orElse(""));
            attributes.put(MESSAGING_STREAM, metadata.stream().orElse(""));
            attributes.put(MESSAGING_SUBJECT, metadata.subject().orElse(""));
        });
        message.getMetadata(ConsumerMetadata.class).ifPresent(metadata -> {
            attributes.put(MESSAGING_STREAM_SEQUENCE, metadata.streamSequence());
            attributes.put(MESSAGING_CONSUMER_SEQUENCE, metadata.consumerSequence());
            attributes.put(MESSAGING_CONSUMER, metadata.consumer());
            attributes.put(MESSAGING_MESSAGE_DELIVERED_COUNT, metadata.deliveredCount());
            attributes.put(MESSAGING_MESSAGE_TIMESTAMP, metadata.timestamp().toString());
        });
    }

    @Override
    public void onEnd(
            AttributesBuilder attributes,
            Context context,
            Message request,
            Void response,
            Throwable error) {
        attributes.put(MESSAGING_MESSAGE_ID, getMessageId(request));
    }

    /**
     * This method is internal and is hence not for public use. Its API is unstable and can change at
     * any time.
     */
    @Override
    public SpanKey internalGetSpanKey() {
        if (operation == null) {
            return null;
        }
        return switch (operation) {
            case PUBLISH -> SpanKey.PRODUCER;
            case RECEIVE -> SpanKey.CONSUMER_RECEIVE;
            case PROCESS -> SpanKey.CONSUMER_PROCESS;
        };
    }

    private String getDestination(Message message) {
        return message.getMetadata(Headers.class)
                .map(metadata -> String.format("%s.%s", metadata.stream().orElse(""), metadata.subject().orElse("")))
                .orElse("");
    }

    private String getMessageId(Message message) {
        return message.getMetadata(Headers.class)
                .flatMap(Headers::messageId).orElse("");
    }
}
