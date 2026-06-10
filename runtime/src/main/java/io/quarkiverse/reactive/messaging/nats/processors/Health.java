package io.quarkiverse.reactive.messaging.nats.processors;

import lombok.Builder;

@Builder
public record Health(boolean healthy, String message) {
}
