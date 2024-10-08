package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;

public interface KeyValueSetupConfiguration {

    /**
     * Bucket Name of Key-Value store
     */
    String bucketName();

    /**
     * Description of Key-Value store
     */
    Optional<String> description();

    /**
     * The storage type (File or Memory).
     */
    StorageType storageType();

    /**
     * The maximum number of bytes for this bucket
     */
    Optional<Long> maxBucketSize();

    /**
     * The maximum number of history for any one key. Includes the current value.
     */
    Optional<Integer> maxHistoryPerKey();

    /**
     * The maximum size for an individual value in the bucket.
     */
    Optional<Integer> maxValueSize();

    /**
     * The maximum age for a value in this bucket
     */
    Optional<Duration> ttl();

    /**
     * The number of replicas for this bucket
     */
    Optional<Integer> replicas();

    /**
     * Sets whether to use compression
     */
    Boolean compressed();

    static List<? extends KeyValueSetupConfiguration> of(JetStreamBuildConfiguration configuration) {
        return configuration.keyValueStores().stream().map(store -> DefaultKeyValueSetupConfiguration.builder()
                .bucketName(store.bucketName())
                .description(store.description())
                .storageType(store.storageType())
                .maxBucketSize(store.maxBucketSize())
                .maxHistoryPerKey(store.maxHistoryPerKey())
                .maxValueSize(store.maxValueSize())
                .ttl(store.ttl().map(Duration::parse))
                .replicas(store.replicas())
                .compressed(store.compressed())
                .build()).toList();
    }

}
