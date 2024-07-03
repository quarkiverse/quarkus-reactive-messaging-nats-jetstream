package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.api.StorageType;
import lombok.Builder;

@Builder
public record DefaultKeyValueSetupConfiguration(
        String name,
        Optional<String> description,
        StorageType storageType,
        Optional<Long> maxBucketSize,
        Optional<Integer> maxHistoryPerKey,
        Optional<Long> maxValueSize,
        Optional<Duration> ttl,
        Optional<Integer> replicas,
        Boolean compressed) implements KeyValueSetupConfiguration {
}
