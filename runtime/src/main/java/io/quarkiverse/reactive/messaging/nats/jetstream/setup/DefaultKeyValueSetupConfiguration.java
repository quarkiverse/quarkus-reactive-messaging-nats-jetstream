package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import io.nats.client.api.StorageType;

import java.time.Duration;
import java.util.Optional;

public class DefaultKeyValueSetupConfiguration implements KeyValueSetupConfiguration {
    private final String name;
    private final String description;
    private final StorageType storageType;
    private final Long maxBucketSize;
    private final Long maxHistoryPerKey;
    private final Long maxValueSize;
    private final Duration ttl;
    private final Integer replicas;
    private final Boolean compressed;

    public DefaultKeyValueSetupConfiguration(String name,
                                             String description,
                                             StorageType storageType,
                                             Long maxBucketSize,
                                             Long maxHistoryPerKey,
                                             Long maxValueSize,
                                             Duration ttl,
                                             Integer replicas,
                                             Boolean compressed) {
        this.name = name;
        this.description = description;
        this.storageType = storageType;
        this.maxBucketSize = maxBucketSize;
        this.maxHistoryPerKey = maxHistoryPerKey;
        this.maxValueSize = maxValueSize;
        this.ttl = ttl;
        this.replicas = replicas;
        this.compressed = compressed;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    @Override
    public StorageType storageType() {
        return storageType;
    }

    @Override
    public Optional<Long> maxBucketSize() {
        return Optional.ofNullable(maxBucketSize);
    }

    @Override
    public Optional<Long> maxHistoryPerKey() {
        return Optional.ofNullable(maxHistoryPerKey);
    }

    @Override
    public Optional<Long> maxValueSize() {
        return Optional.ofNullable(maxValueSize);
    }

    @Override
    public Optional<Duration> ttl() {
        return Optional.ofNullable(ttl);
    }

    @Override
    public Optional<Integer> replicas() {
        return Optional.ofNullable(replicas);
    }

    @Override
    public Boolean compressed() {
        return compressed;
    }
}
