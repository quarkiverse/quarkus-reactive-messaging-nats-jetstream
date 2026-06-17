package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

import java.time.Duration;

@Builder
public record ConsumerLimits(Duration inactiveThreshold,Integer maxAckPending) {
}
