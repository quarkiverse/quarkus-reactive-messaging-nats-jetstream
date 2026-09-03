package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Mirror;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Republish;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Source;
import lombok.Builder;

@Builder
record KeyValueConfigurationImpl(@NonNull String bucketName,
        @NonNull Optional<String> description,
        @NonNull StorageType storageType,
        @NonNull Optional<Long> maxBucketSize,
        int maxHistoryPerKey,
        @NonNull Optional<Duration> ttl,
        int replicas,
        boolean compression,
        @NonNull Optional<Placement> placement,
        @NonNull Map<String, String> metadata,
        @NonNull Optional<Integer> maximumValueSize,
        Optional<Mirror> mirror,
        Optional<Duration> limitMarkerTtl,
        List<Source> sources,
        Optional<Republish> republish) implements KeyValueConfiguration {
}
