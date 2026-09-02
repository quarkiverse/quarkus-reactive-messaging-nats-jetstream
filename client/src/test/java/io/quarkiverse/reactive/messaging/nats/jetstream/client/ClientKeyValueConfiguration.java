package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.Placement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Mirror;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Republish;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Source;

public record ClientKeyValueConfiguration(@NonNull String bucketName,
        @NonNull Optional<String> description) implements KeyValueConfiguration {

    @Override
    public @NonNull StorageType storageType() {
        return StorageType.File;
    }

    @Override
    public @NonNull Optional<Long> maxBucketSize() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Integer> maximumValueSize() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Republish> republish() {
        return Optional.empty();
    }

    @Override
    public int maxHistoryPerKey() {
        return 64;
    }

    @Override
    public @NonNull Optional<Duration> ttl() {
        return Optional.empty();
    }

    @Override
    public int replicas() {
        return 1;
    }

    @Override
    public boolean compression() {
        return false;
    }

    @Override
    public @NonNull Optional<Placement> placement() {
        return Optional.empty();
    }

    @Override
    public @NonNull Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public @NonNull Optional<Mirror> mirror() {
        return Optional.empty();
    }

    @Override
    public @NonNull List<Source> sources() {
        return List.of();
    }

    @Override
    public @NonNull Optional<Duration> limitMarkerTtl() {
        return Optional.empty();
    }
}
