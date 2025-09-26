package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record Payload<T>(T data, Class<T> type) {
}
