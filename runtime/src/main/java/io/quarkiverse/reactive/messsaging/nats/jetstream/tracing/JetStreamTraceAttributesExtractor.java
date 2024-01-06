package io.quarkiverse.reactive.messsaging.nats.jetstream.tracing;

import jakarta.annotation.Nullable;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;

public class JetStreamTraceAttributesExtractor implements AttributesExtractor<JetStreamTrace, Void> {
    private static final String MESSAGE_PAYLOAD = "message.payload";
    private final MessagingAttributesGetter<JetStreamTrace, Void> messagingAttributesGetter;

    public JetStreamTraceAttributesExtractor() {
        this.messagingAttributesGetter = new JetStreamMessagingAttributesGetter();
    }

    @Override
    public void onStart(AttributesBuilder attributesBuilder, Context context, JetStreamTrace jetStreamTrace) {
        attributesBuilder.put(MESSAGE_PAYLOAD, jetStreamTrace.payload());
    }

    @Override
    public void onEnd(AttributesBuilder attributesBuilder, Context context, JetStreamTrace jetStreamTrace,
            @Nullable Void unused, @Nullable Throwable throwable) {

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
            return null;
        }

    }
}
