package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Builder;

@Builder
public record SubscribeMessageMetadata(String stream, String subject, String messageId, Map<String, List<String>> headers) {

    public Optional<String> streamOptional() {
        return Optional.ofNullable(stream);
    }

    public Optional<String> subjectOptional() {
        return Optional.ofNullable(subject);
    }

    public Optional<String> messageIdOptional() {
        return Optional.ofNullable(messageId);
    }

    public Optional<Map<String, List<String>>> headersOptional() {
        return Optional.ofNullable(headers);
    }

    public static SubscribeMessageMetadata of(final String messageId,
            final Map<String, List<String>> headers) {
        return SubscribeMessageMetadata.builder()
                .messageId(messageId)
                .headers(headers)
                .build();
    }

    public static SubscribeMessageMetadata of(final String messageId) {
        return SubscribeMessageMetadata.builder()
                .messageId(messageId)
                .build();
    }

}
