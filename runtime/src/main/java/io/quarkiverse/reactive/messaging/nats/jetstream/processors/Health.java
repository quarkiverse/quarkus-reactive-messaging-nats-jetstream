package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

import lombok.Builder;

@Builder
public record Health(boolean healthy, String message) {
}
