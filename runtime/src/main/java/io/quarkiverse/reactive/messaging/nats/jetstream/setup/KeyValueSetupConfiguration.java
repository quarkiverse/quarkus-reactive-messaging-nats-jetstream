package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

public interface KeyValueSetupConfiguration {

    /**
     * Name of Key-Value store
     */
    String name();

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
    Optional<Long> maxHistoryPerKey();

    /**
     * The maximum size for an individual value in the bucket.
     */
    Optional<Long> maxValueSize();

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

    static KeyValueSetupConfiguration of(JetStreamBuildConfiguration configuration) {

    }

}
