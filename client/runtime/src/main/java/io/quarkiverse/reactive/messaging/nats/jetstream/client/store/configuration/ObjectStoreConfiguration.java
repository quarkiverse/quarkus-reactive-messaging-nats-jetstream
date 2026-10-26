package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.smallrye.config.WithDefault;

/**
 * Defines a configuration interface for an object store.
 * This configuration provides options to control how buckets, storage,
 * replication, and other parameters are managed for an object store.
 */
public interface ObjectStoreConfiguration {

    /**
     * Converts an {@code io.nats.client.api.ObjectStoreConfiguration} instance to an
     * {@code ObjectStoreConfiguration} instance using a mapper.
     *
     * @param configuration the input {@code ObjectStoreConfiguration} instance from the NATS API;
     *        must not be null
     * @return a mapped {@code ObjectStoreConfiguration} instance
     */
    static @NonNull ObjectStoreConfiguration of(io.nats.client.api.@NonNull ObjectStoreConfiguration configuration) {
        final var mapper = Mappers.getMapper(ObjectStoreConfigurationMapper.class);
        return mapper.map(configuration);
    }

    /**
     * Converts an {@link ObjectStoreConfiguration} instance along with a bucket name
     * to an {@code io.nats.client.api.ObjectStoreConfiguration} instance using a mapper.
     *
     * @param configuration the {@link ObjectStoreConfiguration} instance containing the configuration details; must not be null
     * @return a mapped {@code io.nats.client.api.ObjectStoreConfiguration} instance
     */
    static io.nats.client.api.ObjectStoreConfiguration of(@NonNull ObjectStoreConfiguration configuration) {
        final var mapper = Mappers.getMapper(ObjectStoreConfigurationMapper.class);
        return mapper.map(configuration);
    }

    /**
     * Gets the name of the bucket associated with this configuration.
     *
     * @return the name of the bucket, never null.
     */
    @NonNull
    String bucketName();

    /**
     * Provides an optional description for the object store configuration.
     *
     * @return an {@link Optional} containing the description of the configuration, or an empty {@link Optional}
     *         if no description is provided.
     */
    @NonNull
    Optional<String> description();

    /**
     * Retrieves the maximum allowable size, in bytes, for a single bucket in the object store configuration.
     * This value determines the upper limit for the bucket's capacity.
     *
     * @return an {@link Optional} containing the maximum bucket size in bytes, or an empty {@link Optional}
     *         if no size limit is specified.
     */
    @NonNull
    Optional<Long> maxBucketSize();

    /**
     * Retrieves the storage type for the object store configuration.
     * This determines whether storage is file-based or memory-based.
     * If not explicitly specified, the default storage type is {@code File}.
     *
     * @return the storage type, either {@link StorageType#File} or {@link StorageType#Memory}
     */
    @WithDefault("File")
    @NonNull
    StorageType storageType();

    /**
     * Gets the maximum age for a value in this bucket.
     *
     * @return the maximum age.
     */
    @NonNull
    Optional<Duration> ttl();

    /**
     * Gets the number of replicas for this stream configuration.
     *
     * @return the number of replicas
     */
    @WithDefault("1")
    int replicas();

    /**
     * Indicates whether compression is enabled for this configuration.
     *
     * @return {@code true} if compression is enabled, {@code false} otherwise.
     */
    @WithDefault("false")
    boolean compression();

    /**
     * Placement directives to consider when placing replicas of this stream,
     * random placement when unset
     *
     * @return the placement [directive object]
     */
    @NonNull
    Optional<Placement> placement();

    /**
     * Retrieves the metadata associated with this configuration.
     * The metadata is represented as a map where keys and values are both strings.
     *
     * @return a non-null map containing metadata as key-value pairs
     */
    @NonNull
    Map<String, String> metadata();

}
