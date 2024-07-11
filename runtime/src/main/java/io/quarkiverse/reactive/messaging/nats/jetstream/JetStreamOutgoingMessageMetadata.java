package io.quarkiverse.reactive.messaging.nats.jetstream;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.Builder;

@Builder
public record JetStreamOutgoingMessageMetadata(String messageId, Map<String, List<String>> headers,
        Optional<String> subtopic) {

    public static JetStreamOutgoingMessageMetadata of(final String messageId,
            final Map<String, List<String>> headers,
            final String subtopic) {
        return JetStreamOutgoingMessageMetadata.builder()
                .messageId(messageId)
                .headers(headers != null ? headers : Collections.emptyMap())
                .subtopic(Optional.ofNullable(subtopic)).build();
    }

    public static JetStreamOutgoingMessageMetadata of(final String messageId,
            final Map<String, List<String>> headers) {
        return JetStreamOutgoingMessageMetadata.builder()
                .messageId(messageId)
                .headers(headers != null ? headers : Collections.emptyMap())
                .subtopic(Optional.empty()).build();
    }

    public static JetStreamOutgoingMessageMetadata of(final String messageId) {
        return JetStreamOutgoingMessageMetadata.builder()
                .messageId(messageId)
                .headers(Collections.emptyMap())
                .subtopic(Optional.empty()).build();
    }

}
