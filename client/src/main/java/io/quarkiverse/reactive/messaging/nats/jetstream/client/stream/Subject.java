package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

@Builder
public record Subject(String name, long count) {
}
