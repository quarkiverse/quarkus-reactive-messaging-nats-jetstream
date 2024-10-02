package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.time.Duration;

import lombok.Builder;

@Builder
public record Replica(String name,
        Boolean current,
        Boolean offline,
        Duration active,
        Long lag) {
}
