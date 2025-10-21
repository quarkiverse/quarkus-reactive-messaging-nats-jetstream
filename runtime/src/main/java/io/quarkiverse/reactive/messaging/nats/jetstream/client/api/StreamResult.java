package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration;
import lombok.Builder;

@Builder
public record StreamResult(StreamConfiguration configuration, StreamStatus status) {
}
