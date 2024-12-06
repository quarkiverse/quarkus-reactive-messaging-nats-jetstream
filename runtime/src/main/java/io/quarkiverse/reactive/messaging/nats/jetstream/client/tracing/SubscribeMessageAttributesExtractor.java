package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessage;

public class SubscribeMessageAttributesExtractor<T> implements AttributesExtractor<SubscribeMessage<T>, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";

    private final MessagingAttributesGetter<SubscribeMessage<T>, Void> attributesGetter;

    public SubscribeMessageAttributesExtractor() {
        this.attributesGetter = new SubscribeMessagingAttributesGetter<>();
    }

    @Override
    public void onStart(AttributesBuilder attributes, Context parentContext, SubscribeMessage<T> message) {
        attributes.put(MESSAGE_PAYLOAD, new String(message.payload()));
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, SubscribeMessage<T> tSubscribeMessage, Void unused,
            Throwable error) {

    }

    public MessagingAttributesGetter<SubscribeMessage<T>, Void> getMessagingAttributesGetter() {
        return attributesGetter;
    }

    private final static class SubscribeMessagingAttributesGetter<T>
            implements MessagingAttributesGetter<SubscribeMessage<T>, Void> {

        @Override
        public String getSystem(SubscribeMessage<T> message) {
            return "jetstream";
        }

        @Override
        public String getDestination(SubscribeMessage<T> message) {
            return String.format("%s.%s", message.configuration().stream(), message.configuration().subject());
        }

        @Override
        public boolean isTemporaryDestination(SubscribeMessage<T> message) {
            return false;
        }

        @Override
        public String getConversationId(SubscribeMessage<T> message) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(SubscribeMessage<T> message) {
            return (long) message.payload().length;
        }

        @Override
        public Long getMessagePayloadCompressedSize(SubscribeMessage<T> message) {
            return null;
        }

        @Override
        public String getMessageId(SubscribeMessage<T> message, Void unused) {
            return message.messageId();
        }

    }
}
