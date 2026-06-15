package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;

import lombok.Builder;

@Builder
public record Replica(String name,
                      Boolean current,
                      Boolean offline,
                      Duration active,
                      Long lag) {
}
