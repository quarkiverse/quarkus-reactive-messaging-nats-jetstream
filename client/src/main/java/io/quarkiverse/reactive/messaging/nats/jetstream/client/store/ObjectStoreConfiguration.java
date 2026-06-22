package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Builder
public record ObjectStoreConfiguration(        /* Name of Key-Value store */
        @NonNull String name,
        /* Description of Key-Value store */
        @NonNull Optional<String> description,
        /* The storage type (File or Memory). */
        @NonNull StorageType storageType,
        /* The maximum number of bytes for this bucket */
        @NonNull Optional<Long> maxBucketSize,
        /* The maximum number of history for any one key. Includes the current value. */
        @NonNull Optional<Integer> maxHistoryPerKey,
        /* The maximum size for an individual value in the bucket. */
        @NonNull Optional<Integer> maxValueSize,
        /* The maximum age for a value in this bucket */
        @NonNull Optional<Duration> ttl,
        /* The number of replicas for this bucket */
        @NonNull Optional<Integer> replicas,
        /* Sets whether to use compression */
        @NonNull Optional<Boolean> compressed,
        /* The placement directive */
        @NonNull Optional<Placement> placement) {

    public ObjectStoreConfiguration {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(storageType, "storageType");
        Objects.requireNonNull(maxBucketSize, "maxBucketSize");
        Objects.requireNonNull(maxHistoryPerKey, "maxHistoryPerKey");
        Objects.requireNonNull(maxValueSize, "maxValueSize");
        Objects.requireNonNull(ttl, "ttl");
        Objects.requireNonNull(replicas, "replicas");
        Objects.requireNonNull(compressed, "compressed");
        Objects.requireNonNull(placement, "placement");
    }
}
