package io.quarkiverse.reactive.messaging.nats.jetstream.tracing;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;

public class JetStreamTraceAttributesExtractor implements AttributesExtractor<JetStreamTrace, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private static final String MESSAGE_STREAM_SEQUENCE = "message.stream_sequence";
    private static final String MESSAGE_CONSUMER_SEQUENCE = "message.consumer_sequence";
    private static final String MESSAGE_CONSUMER = "message.consumer";
    private static final String MESSAGE_DELIVERED_COUNT = "message.delivered_count";

    private final MessagingAttributesGetter<JetStreamTrace, Void> messagingAttributesGetter;

    public JetStreamTraceAttributesExtractor() {
        this.messagingAttributesGetter = new JetStreamMessagingAttributesGetter();
    }

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, JetStreamTrace jetStreamTrace) {
        attributesBuilder.put(MESSAGE_PAYLOAD, jetStreamTrace.payload());
        if (jetStreamTrace.streamSequence() != null) {
            attributesBuilder.put(MESSAGE_STREAM_SEQUENCE, jetStreamTrace.streamSequence());
        }
        if (jetStreamTrace.consumerSequence() != null) {
            attributesBuilder.put(MESSAGE_CONSUMER_SEQUENCE, jetStreamTrace.consumerSequence());
        }
        if (jetStreamTrace.consumer() != null) {
            attributesBuilder.put(MESSAGE_CONSUMER, jetStreamTrace.consumer());
        }
        if (jetStreamTrace.deliveredCount() != null) {
            attributesBuilder.put(MESSAGE_DELIVERED_COUNT, jetStreamTrace.deliveredCount());
        }
    }

    @Override
    public void onEnd(AttributesBuilder attributesBuilder, Context context, JetStreamTrace jetStreamTrace,
            Void unused, Throwable throwable) {
    }

    public MessagingAttributesGetter<JetStreamTrace, Void> getMessagingAttributesGetter() {
        return messagingAttributesGetter;
    }

    private final static class JetStreamMessagingAttributesGetter implements MessagingAttributesGetter<JetStreamTrace, Void> {

        @Override
        public String getSystem(JetStreamTrace trace) {
            return "jetstream";
        }

        @Override
        public String getDestination(JetStreamTrace trace) {
            return String.format("%s.%s", trace.stream(), trace.subject());
        }

        @Override
        public boolean isTemporaryDestination(JetStreamTrace trace) {
            return false;
        }

        @Override
        public String getConversationId(JetStreamTrace trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadSize(JetStreamTrace trace) {
            return null;
        }

        @Override
        public Long getMessagePayloadCompressedSize(JetStreamTrace trace) {
            return null;
        }

        @Override
        public String getMessageId(JetStreamTrace trace, Void unused) {
            return trace.messageId();
        }

    }
}
