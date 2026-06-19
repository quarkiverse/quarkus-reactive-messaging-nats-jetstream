package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

import java.time.Duration;

@Builder
public record Replica(String name,
                      boolean current,
                      boolean offline,
                      Duration active,
                      long lag) {
}
