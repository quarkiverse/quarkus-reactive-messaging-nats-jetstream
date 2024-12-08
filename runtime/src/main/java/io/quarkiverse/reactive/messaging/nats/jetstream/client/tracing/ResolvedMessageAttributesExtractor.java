package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.ResolvedMessage;

public class ResolvedMessageAttributesExtractor<T> implements AttributesExtractor<ResolvedMessage<T>, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";

    private final MessagingAttributesGetter<ResolvedMessage<T>, Void> messagingAttributesGetter;

    public ResolvedMessageAttributesExtractor() {
        this.messagingAttributesGetter = new ResolvedMessageAttributesGetter<>();
    }

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, ResolvedMessage<T> message) {
        attributesBuilder.put(MESSAGE_PAYLOAD, new String(message.getData()));
        attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, message.messageInfo().getSeq());
    }

    @Override
    public void onEnd(AttributesBuilder attributes, Context context, ResolvedMessage<T> message, Void unused, Throwable error) {

    }

    public MessagingAttributesGetter<ResolvedMessage<T>, Void> getMessagingAttributesGetter() {
        return messagingAttributesGetter;
    }

    private final static class ResolvedMessageAttributesGetter<T>
            implements MessagingAttributesGetter<ResolvedMessage<T>, Void> {

        @Override
        public String getSystem(ResolvedMessage<T> trace) {
            return "jetstream";
        }

        @Override
        public String getDestination(ResolvedMessage<T> trace) {
            return String.format("%s.%s", trace.getStream(), trace.getSubject());
        }

        @Override
        public boolean isTemporaryDestination(ResolvedMessage<T> trace) {
            return false;
        }

        @Override
        public String getConversationId(ResolvedMessage<T> trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(ResolvedMessage<T> trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadCompressedSize(ResolvedMessage<T> trace) {
            return null;
        }

        @Override
        public String getMessageId(ResolvedMessage<T> trace, Void unused) {
            return trace.messageId();
        }
    }
}
