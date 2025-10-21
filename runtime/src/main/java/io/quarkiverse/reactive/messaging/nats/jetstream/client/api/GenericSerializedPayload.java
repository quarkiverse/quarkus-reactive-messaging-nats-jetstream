package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record GenericSerializedPayload<T>(byte[] data, Class<T> type) implements SerializedPayload<T> {
}
