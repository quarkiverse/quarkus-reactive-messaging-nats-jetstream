package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.nio.charset.StandardCharsets;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingAttributesGetter;

public class SubscribeMessageAttributesExtractor implements AttributesExtractor<SubscribeMessageMetadata, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    private static final String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    private static final String MESSAGE_CONSUMER = "message.consumer";
    private static final String MESSAGE_DELIVERED_COUNT = "message.delivered_count";

    private final MessagingAttributesGetter<SubscribeMessageMetadata> attributesGetter;

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

    public MessagingAttributesGetter<SubscribeMessageMetadata> getMessagingAttributesGetter() {
        return attributesGetter;
    }

    private final static class SubscribeMessagingAttributesGetter
            implements MessagingAttributesGetter<SubscribeMessageMetadata> {

        @Override
        public String getSystem(SubscribeMessageMetadata metadata) {
            return "jetstream";
        }

        @Override
        public String getDestination(SubscribeMessageMetadata metadata) {
            return String.format("%s.%s", metadata.stream(), metadata.subject());
        }

        @Override
        public Long getMessageBodySize(SubscribeMessageMetadata metadata) {
            return (long) metadata.payload().length;
        }

        @Override
        public String getMessageId(SubscribeMessageMetadata metadata) {
            return metadata.messageId();
        }
    }
}
