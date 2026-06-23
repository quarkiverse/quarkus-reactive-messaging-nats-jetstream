package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.Duration;

import lombok.Builder;

@Builder
public record Replica(String name,
        boolean current,
        boolean offline,
        Duration active,
        long lag) {
}
