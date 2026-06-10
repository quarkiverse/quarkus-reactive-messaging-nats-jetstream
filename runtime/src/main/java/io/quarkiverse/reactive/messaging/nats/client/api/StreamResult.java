package io.quarkiverse.reactive.messaging.nats.client.api;

import io.quarkiverse.reactive.messaging.nats.client.stream.StreamConfiguration;
import lombok.Builder;

@Builder
public record StreamResult(StreamConfiguration configuration, StreamStatus status) {
}
