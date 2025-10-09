package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import java.nio.charset.StandardCharsets;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;

public class PublishMessageAttributesExtractor implements AttributesExtractor<PublishMessageMetadata, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_TYPE = "message.type";

    private final MessagingAttributesGetter<PublishMessageMetadata, Void> attributesGetter;

    public PublishMessageAttributesExtractor() {
        this.attributesGetter = new PublishMessagingAttributesGetter();
    }

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, PublishMessageMetadata message) {
        attributes.put(MESSAGE_PAYLOAD, new String(message.payload().data(), StandardCharsets.UTF_8));
        attributes.put(MESSAGE_TYPE, message.payload().type().toString());
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, PublishMessageMetadata tSubscribeMessage, Void unused,
            Throwable error) {

    }

    public MessagingAttributesGetter<PublishMessageMetadata, Void> getMessagingAttributesGetter() {
        return attributesGetter;
    }

    private final static class PublishMessagingAttributesGetter
            implements MessagingAttributesGetter<PublishMessageMetadata, Void> {

        @Override
        public String getSystem(PublishMessageMetadata metadata) {
            return "jetstream";
        }

        @Override
        public String getDestination(PublishMessageMetadata metadata) {
            return String.format("%s.%s", metadata.stream(), metadata.subject());
        }

        @Override
        public boolean isTemporaryDestination(PublishMessageMetadata metadata) {
            return false;
        }

        @Override
        public String getConversationId(PublishMessageMetadata message) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(PublishMessageMetadata message) {
            return (long) message.payload().data().length;
        }

        @Override
        public Long getMessagePayloadCompressedSize(PublishMessageMetadata message) {
            return null;
        }

        @Override
        public String getMessageId(PublishMessageMetadata message, Void unused) {
            return message.payload().id();
        }

    }
}
