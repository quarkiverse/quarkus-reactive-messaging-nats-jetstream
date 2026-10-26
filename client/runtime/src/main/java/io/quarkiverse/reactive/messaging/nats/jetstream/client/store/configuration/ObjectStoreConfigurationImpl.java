package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public record ObjectStoreConfigurationImpl(@NonNull String bucketName,
        @NonNull Optional<String> description,
        @NonNull StorageType storageType,
        @NonNull Optional<Long> maxBucketSize,
        @NonNull Optional<Duration> ttl,
        int replicas,
        boolean compression,
        @NonNull Optional<Placement> placement,
        @NonNull Map<String, String> metadata) implements ObjectStoreConfiguration {
}
