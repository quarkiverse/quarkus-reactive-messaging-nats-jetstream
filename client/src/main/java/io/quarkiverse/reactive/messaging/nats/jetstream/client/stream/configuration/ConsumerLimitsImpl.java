package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.Duration;
import java.util.Optional;

import lombok.Builder;
import lombok.NonNull;

@Builder
record ConsumerLimitsImpl(long maxAckPending, @NonNull Optional<Duration> inactiveThreshold) implements ConsumerLimits {
}
