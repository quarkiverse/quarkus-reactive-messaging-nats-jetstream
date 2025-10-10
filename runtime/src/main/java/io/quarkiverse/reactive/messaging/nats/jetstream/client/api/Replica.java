package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

import java.time.Duration;

@Builder
public record Replica(String name,
        Boolean current,
        Boolean offline,
        Duration active,
        Long lag) {
}
