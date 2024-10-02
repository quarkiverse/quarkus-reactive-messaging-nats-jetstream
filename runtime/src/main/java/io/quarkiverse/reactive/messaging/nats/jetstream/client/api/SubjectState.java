package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record SubjectState(String name, long count) {
}
