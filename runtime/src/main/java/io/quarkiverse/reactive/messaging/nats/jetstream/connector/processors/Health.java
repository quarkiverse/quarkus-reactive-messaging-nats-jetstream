package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import lombok.Builder;

@Builder
public record Health(boolean healthy, String message) {
}
