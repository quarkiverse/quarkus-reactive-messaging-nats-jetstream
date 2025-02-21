package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.nio.charset.StandardCharsets;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;

public class SubscribeMessageAttributesExtractor implements AttributesExtractor<SubscribeMessageMetadata, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    private static final String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    private static final String MESSAGE_CONSUMER = "message.consumer";
    private static final String MESSAGE_DELIVERED_COUNT = "message.delivered_count";

    private final MessagingAttributesGetter<SubscribeMessageMetadata, Void> attributesGetter;

    public SubscribeMessageAttributesExtractor() {
        this.attributesGetter = new SubscribeMessagingAttributesGetter();
    }

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, SubscribeMessageMetadata metadata) {
        attributesBuilder.put(MESSAGE_PAYLOAD, new String(metadata.payload(), StandardCharsets.UTF_8));
        attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, metadata.streamSequence());
        attributesBuilder.put(MESSAGE_CONSUMER_SEQUENCE, metadata.consumerSequence());
        attributesBuilder.put(MESSAGE_CONSUMER, metadata.consumer());
        attributesBuilder.put(MESSAGE_DELIVERED_COUNT, metadata.deliveredCount());
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, SubscribeMessageMetadata metadata, Void unused,
            Throwable error) {
    }

    public MessagingAttributesGetter<SubscribeMessageMetadata, Void> getMessagingAttributesGetter() {
        return attributesGetter;
    }

    private final static class SubscribeMessagingAttributesGetter
            implements MessagingAttributesGetter<SubscribeMessageMetadata, Void> {

        @Override
        public String getSystem(SubscribeMessageMetadata metadata) {
            return "jetstream";
        }

        @Override
        public String getDestination(SubscribeMessageMetadata metadata) {
            return String.format("%s.%s", metadata.stream(), metadata.subject());
        }

        @Override
        public String getDestinationKind(SubscribeMessageMetadata metadata) {
            return "Stream";
        }

        @Override
        public boolean isTemporaryDestination(SubscribeMessageMetadata metadata) {
            return false;
        }

        @Override
        public String getConversationId(SubscribeMessageMetadata message) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(SubscribeMessageMetadata metadata) {
            return (long) metadata.payload().length;
        }

        @Override
        public Long getMessagePayloadCompressedSize(SubscribeMessageMetadata message) {
            return null;
        }

        @Override
        public String getMessageId(SubscribeMessageMetadata message, Void unused) {
            return message.messageId();
        }
    }
}
