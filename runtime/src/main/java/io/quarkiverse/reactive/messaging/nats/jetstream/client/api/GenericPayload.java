package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record GenericPayload<P, T>(P data, Class<T> type) implements Payload<P, T> {
}
