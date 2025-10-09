package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import lombok.Builder;

import java.time.Duration;
import java.util.Optional;

@Builder
public record PullConfigurationImpl(Duration maxExpires, Integer batchSize, Integer rePullAt, Optional<Integer> maxWaiting) implements PullConfiguration {
}
