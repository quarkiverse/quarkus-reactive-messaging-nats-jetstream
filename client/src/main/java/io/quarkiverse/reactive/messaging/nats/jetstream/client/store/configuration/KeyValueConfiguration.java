package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Mirror;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Republish;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Source;
import io.smallrye.config.WithDefault;

/**
 * Represents the configuration for a key-value bucket in a NATS JetStream context.
 * The configuration includes various properties such as storage type, compression type,
 * size limitations, value expiration settings, and replication details.
 */
public interface KeyValueConfiguration {

    /**
     * Creates a new instance of {@link KeyValueConfiguration} from the given NATS client API KeyValueConfiguration.
     * This method utilizes a mapper to map the provided NATS configuration to the custom KeyValueConfiguration implementation.
     *
     * @param configuration the NATS client API KeyValueConfiguration to be transformed, must not be null
     * @return a new instance of KeyValueConfiguration created from the provided configuration
     */
    static @NonNull KeyValueConfiguration of(io.nats.client.api.@NonNull KeyValueConfiguration configuration) {
        final var mapper = Mappers.getMapper(KeyValueConfigurationMapper.class);
        return mapper.map(configuration);
    }

    /**
     * Creates an instance of {@link io.nats.client.api.KeyValueConfiguration} based on the provided bucket name
     * and a custom {@link KeyValueConfiguration} object. This method utilizes a mapper to transform the custom
     * configuration into a NATS client API {@link io.nats.client.api.KeyValueConfiguration}.
     *
     * @param configuration the custom KeyValueConfiguration object containing the configuration details, must not be null
     * @return a new {@link io.nats.client.api.KeyValueConfiguration} instance initialized with the provided bucket name
     *         and configuration properties
     */
    static io.nats.client.api.KeyValueConfiguration of(@NonNull KeyValueConfiguration configuration) {
        final var mapper = Mappers.getMapper(KeyValueConfigurationMapper.class);
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
     * Gets the description of this bucket.
     *
     * @return the description of the bucket.
     */
    @NonNull
    Optional<String> description();

    /**
     * Gets the storage type for this bucket.
     *
     * @return the storage type for this key/value.
     */
    @WithDefault("File")
    @NonNull
    StorageType storageType();

    /**
     * Gets the maximum number of bytes for this bucket.
     *
     * @return the maximum number of bytes for this bucket.
     */
    @NonNull
    Optional<Long> maxBucketSize();

    /**
     * Gets the maximum size for an individual value in the bucket.
     *
     * @return the maximum size for a value.
     */
    @NonNull
    Optional<Integer> maximumValueSize();

    /**
     * Get the republish configuration. Might be null.
     *
     * @return the republish object
     */
    @NonNull
    Optional<Republish> republish();

    /**
     * Gets the maximum number of history for any one key. Includes the current value.
     * Max History must be from 1 to 64 inclusive
     *
     * @return the maximum number of values for any one key.
     */
    @WithDefault("64")
    int maxHistoryPerKey();

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

    /**
     * The mirror definition for this configuration
     *
     * @return the mirror
     */
    @NonNull
    Optional<Mirror> mirror();

    /**
     * The sources for this configuration
     *
     * @return the sources
     */

    @NonNull
    List<Source> sources();

    /**
     * The limit marker ttl if set
     *
     * @return the duration
     */
    @NonNull
    Optional<Duration> limitMarkerTtl();

}
