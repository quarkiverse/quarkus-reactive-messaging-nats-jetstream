package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.internal.SpanKey;
import io.opentelemetry.instrumentation.api.internal.SpanKeyProvider;
import io.quarkiverse.reactive.nats.jetstream.message.Message;

public final class MessageAttributesExtractor implements AttributesExtractor<Message, Void>, SpanKeyProvider {
    private static final AttributeKey<String> MESSAGING_DESTINATION_NAME = AttributeKey.stringKey("messaging.destination.name");
    private static final AttributeKey<Long> MESSAGING_MESSAGE_BODY_SIZE = AttributeKey.longKey("messaging.message.body.size");
    private static final AttributeKey<String> MESSAGING_MESSAGE_ID = AttributeKey.stringKey("messaging.message.id");
    private static final AttributeKey<String> MESSAGING_OPERATION = AttributeKey.stringKey("messaging.operation");
    private static final AttributeKey<String> MESSAGING_SYSTEM = AttributeKey.stringKey("messaging.system");

    /**
     * Creates the messaging attributes extractor for the given {@link MessageOperation operation}
     * with default configuration.
     */
    public static AttributesExtractor<Message, Void> create(
            MessageInfo getter, MessageOperation operation) {
        return new MessageAttributesExtractor(getter, operation);
    }

    private final MessageInfo getter;
    private final MessageOperation operation;

    MessageAttributesExtractor(
            MessageInfo getter,
            MessageOperation operation) {
        this.getter = getter;
        this.operation = operation;
    }

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, Message message) {
        attributes.put(MESSAGING_SYSTEM, getter.getSystem(message));
        attributes.put(MESSAGING_DESTINATION_NAME, getter.getDestination(message));
        attributes.put(MESSAGING_MESSAGE_BODY_SIZE, getter.getMessageBodySize(message));
        attributes.put(MESSAGING_OPERATION, operation.toString());
    }

    @Override
    public void onEnd(
            AttributesBuilder attributes,
            Context context,
            Message request,
            Void response,
            Throwable error) {
        attributes.put(MESSAGING_MESSAGE_ID, getter.getMessageId(request));
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
}
