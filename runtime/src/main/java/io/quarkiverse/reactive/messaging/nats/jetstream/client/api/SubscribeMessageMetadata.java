package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record SubscribeMessageMetadata(String messageId, Map<String, List<String>> headers) {

    public static SubscribeMessageMetadata of(final String messageId,
            final Map<String, List<String>> headers) {
        return SubscribeMessageMetadata.builder()
                .messageId(messageId)
                .headers(headers != null ? headers : Collections.emptyMap())
                .build();
    }

    public static SubscribeMessageMetadata of(final String messageId) {
        return SubscribeMessageMetadata.builder()
                .messageId(messageId)
                .headers(Collections.emptyMap())
                .build();
    }

}
