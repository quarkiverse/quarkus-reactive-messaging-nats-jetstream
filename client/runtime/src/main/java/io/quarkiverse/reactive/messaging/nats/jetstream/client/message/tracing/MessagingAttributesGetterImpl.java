package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers;

/**
 * Provides the standard messaging attributes of a NATS JetStream message to the OpenTelemetry messaging semantic
 * conventions.
 */
record MessagingAttributesGetterImpl(@NonNull Operation operation) implements MessagingAttributesGetter<Message<byte[]>, Void> {
    static final String SYSTEM = "nats";

    @Override
    public String getSystem(Message<byte[]> message) {
        return SYSTEM;
    }

    @Override
    public @Nullable String getDestination(Message<byte[]> message) {
        return headers(message).flatMap(Headers::subject).orElse(null);
    }

    @Override
    public @Nullable String getDestinationTemplate(Message<byte[]> message) {
        return null;
    }

    @Override
    public boolean isTemporaryDestination(Message<byte[]> message) {
        return false;
    }

    @Override
    public boolean isAnonymousDestination(Message<byte[]> message) {
        return false;
    }

    @Override
    public @Nullable String getConversationId(Message<byte[]> message) {
        return headers(message).flatMap(Headers::correlationId).orElse(null);
    }

    @Override
    public @Nullable Long getMessageBodySize(Message<byte[]> message) {
        return message.getPayload() != null ? (long) message.getPayload().length : null;
    }

    @Override
    public @Nullable Long getMessageEnvelopeSize(Message<byte[]> message) {
        return null;
    }

    @Override
    public @Nullable String getMessageId(Message<byte[]> message, @Nullable Void unused) {
        return headers(message).flatMap(Headers::messageId).orElse(null);
    }

    @Override
    public @Nullable String getClientId(Message<byte[]> message) {
        return null;
    }

    @Override
    public @Nullable Long getBatchMessageCount(Message<byte[]> message, @Nullable Void unused) {
        return null;
    }

    @Override
    public List<String> getMessageHeader(Message<byte[]> message, String name) {
        return headers(message).map(headers -> headers.getOrDefault(name, List.of())).orElse(List.of());
    }

    private Optional<Headers> headers(Message<byte[]> message) {
        return TracedHeaders.of(operation, message);
    }
}
