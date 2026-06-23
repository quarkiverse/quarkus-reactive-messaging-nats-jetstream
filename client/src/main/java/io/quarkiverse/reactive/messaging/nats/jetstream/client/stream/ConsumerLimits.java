package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.Duration;

import lombok.Builder;

@Builder
public record ConsumerLimits(Duration inactiveThreshold, Integer maxAckPending) {
}
