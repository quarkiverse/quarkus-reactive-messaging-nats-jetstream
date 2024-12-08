package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessage;

@SuppressWarnings("LombokGetterMayBeUsed")
public class PublishMessageAttributesExtractor<T> implements AttributesExtractor<PublishMessage<T>, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    private static final String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    private static final String MESSAGE_CONSUMER = "message.consumer";
    private static final String MESSAGE_DELIVERED_COUNT = "message.delivered_count";

    private final MessagingAttributesGetter<PublishMessage<T>, Void> messagingAttributesGetter;

    public PublishMessageAttributesExtractor() {
        this.messagingAttributesGetter = new PublishMessageAttributesGetter<>();
    }

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, PublishMessage<T> message) {
        attributesBuilder.put(MESSAGE_PAYLOAD, new String(message.getData()));
        attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, message.getStreamSequence());
        attributesBuilder.put(MESSAGE_CONSUMER_SEQUENCE, message.getConsumerSequence());
        attributesBuilder.put(MESSAGE_CONSUMER, message.getConsumer());
        attributesBuilder.put(MESSAGE_DELIVERED_COUNT, message.getDeliveredCount());
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, PublishMessage<T> message, Void unused, Throwable error) {

    }

    public MessagingAttributesGetter<PublishMessage<T>, Void> getMessagingAttributesGetter() {
        return messagingAttributesGetter;
    }

    private final static class PublishMessageAttributesGetter<T>
            implements MessagingAttributesGetter<PublishMessage<T>, Void> {

        @Override
        public String getSystem(PublishMessage<T> trace) {
            return "jetstream";
        }

        @Override
        public String getDestination(PublishMessage<T> trace) {
            return String.format("%s.%s", trace.getStream(), trace.getSubject());
        }

        @Override
        public boolean isTemporaryDestination(PublishMessage<T> trace) {
            return false;
        }

        @Override
        public String getConversationId(PublishMessage<T> trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(PublishMessage<T> trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadCompressedSize(PublishMessage<T> trace) {
            return null;
        }

        @Override
        public String getMessageId(PublishMessage<T> trace, Void unused) {
            return trace.messageId();
        }

    }
}
