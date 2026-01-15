package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.api.StorageType;

public interface KeyValueStoreConfiguration {

    /**
     * Name of Key-Value store. If not set then the key of the map is used
     */
    Optional<String> bucketName();

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
    Optional<Boolean> compressed();
}
