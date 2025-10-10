package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;
import java.util.Optional;

import lombok.Builder;

@Builder
public record PullConfigurationImpl(Duration maxExpires, Integer batchSize, Integer rePullAt,
        Optional<Integer> maxWaiting) implements PullConfiguration {
}
