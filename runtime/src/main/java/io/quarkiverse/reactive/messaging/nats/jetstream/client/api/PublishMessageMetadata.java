package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record PublishMessageMetadata(String stream,
        String subject,
        SerializedPayload<?> payload,
        Long sequence) {
}
