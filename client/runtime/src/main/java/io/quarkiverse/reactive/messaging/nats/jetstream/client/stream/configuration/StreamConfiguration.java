package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.smallrye.config.WithDefault;

/**
 * The StreamConfiguration class specifies the configuration for creating a JetStream stream on the server.
 */
public interface StreamConfiguration {

    /**
     * Converts an instance of {@link io.nats.client.api.StreamConfiguration} to an instance of {@code StreamConfiguration}.
     *
     * @param configuration the source {@link io.nats.client.api.StreamConfiguration} to be converted.
     * @return a mapped {@code StreamConfiguration} created from the provided {@link io.nats.client.api.StreamConfiguration}.
     */
    static StreamConfiguration of(io.nats.client.api.StreamConfiguration configuration) {
        final var mapper = Mappers.getMapper(StreamConfigurationMapper.class);
        return mapper.map(configuration);
    }

    /**
     * Maps the provided stream name and stream configuration into an instance of
     * {@link io.nats.client.api.StreamConfiguration}.
     *
     * @param configuration the source {@link StreamConfiguration} containing the configuration details.
     * @return a mapped {@link io.nats.client.api.StreamConfiguration} created from the provided stream name
     *         and configuration.
     */
    static io.nats.client.api.StreamConfiguration of(StreamConfiguration configuration) {
        final var mapper = Mappers.getMapper(StreamConfigurationMapper.class);
        return mapper.map(configuration);
    }

    static StreamConfiguration of(StreamConfiguration configuration, List<String> subjects) {
        final var mapper = Mappers.getMapper(StreamConfigurationMapper.class);
        return mapper.map(configuration, subjects);
    }

    /**
     * Retrieves the name as a non-null string.
     *
     * @return A non-null string representing the name.
     */
    @NonNull
    String name();

    /**
     * Gets the retention policy for this stream configuration.
     *
     * @return the retention policy for this stream.
     */
    @WithDefault("Interest")
    @NonNull
    RetentionPolicy retentionPolicy();

    /**
     * Gets the compression option for this stream configuration.
     *
     * @return the compression option for this stream.
     */
    @WithDefault("None")
    @NonNull
    Compression compression();

    /**
     * Gets the storage type for this stream configuration.
     *
     * @return the storage type for this stream.
     */
    @WithDefault("File")
    @NonNull
    StorageType storageType();

    /**
     * Gets the discard policy for this stream configuration.
     *
     * @return the discard policy of the stream.
     */
    @NonNull
    @WithDefault("Old")
    DiscardPolicy discardPolicy();

    /**
     * Gets the description of this stream configuration.
     *
     * @return the description of the stream.
     */
    @NonNull
    Optional<String> description();

    /**
     * Gets the maximum number of consumers for this stream configuration.
     *
     * @return the maximum number of consumers for this stream.
     */
    @NonNull
    Optional<Long> maxConsumers();

    /**
     * Gets the maximum messages for this stream configuration.
     *
     * @return the maximum number of messages for this stream.
     */
    @NonNull
    Optional<Long> maxMessages();

    /**
     * Gets the maximum messages for this stream configuration.
     *
     * @return the maximum number of messages for this stream.
     */
    @NonNull
    Optional<Long> maxMessagesPerSubject();

    /**
     * Gets the maximum number of bytes for this stream configuration.
     *
     * @return the maximum number of bytes for this stream.
     */
    @NonNull
    Optional<Long> maxBytes();

    /**
     * Gets the maximum message age for this stream configuration.
     *
     * @return the maximum message age for this stream.
     */
    @NonNull
    Optional<Duration> maxAge();

    /**
     * Gets the maximum message size for this stream configuration.
     *
     * @return the maximum message size for this stream.
     */
    @NonNull
    Optional<Integer> maximumMessageSize();

    /**
     * Gets the number of replicas for this stream configuration.
     *
     * @return the number of replicas
     */
    @WithDefault("1")
    int replicas();

    /**
     * Gets whether acknowledgements are required in this stream configuration.
     *
     * @return true if acknowedgments are not required.
     */
    @WithDefault("true")
    boolean noAck();

    /**
     * Gets the template json for this stream configuration.
     *
     * @return the template for this stream.
     */
    @NonNull
    Optional<String> templateOwner();

    /**
     * Gets the duplicate checking window stream configuration. Duration.ZERO
     * means duplicate checking is not enabled.
     *
     * @return the duration of the window.
     */
    @NonNull
    Optional<Duration> duplicateWindow();

    /**
     * Gets the subjects for this stream configuration.
     *
     * @return the subject of the stream.
     */
    @NonNull
    Set<String> subjects();

    /**
     * Get the placement directives to consider when placing replicas of this stream,
     * random placement when unset. May be null.
     *
     * @return the placement object
     */
    @NonNull
    Optional<Placement> placement();

    /**
     * Get the republish configuration. May be null.
     *
     * @return the republish object
     */
    @NonNull
    Optional<Republish> republish();

    /**
     * Get the subjectTransform configuration. May be null.
     *
     * @return the subjectTransform object
     */
    @NonNull
    Optional<SubjectTransform> subjectTransform();

    /**
     * Get the consumerLimits configuration. May be null.
     *
     * @return the consumerLimits object
     */
    @NonNull
    Optional<ConsumerLimits> consumerLimits();

    /**
     * The mirror definition for this stream
     *
     * @return the mirror
     */
    @NonNull
    Optional<Mirror> mirror();

    /**
     * The sources for this stream
     *
     * @return the sources
     */
    @NonNull
    @WithDefault("")
    List<Source> sources();

    /**
     * Get the flag indicating if the stream is sealed.
     *
     * @return the sealed flag
     */
    @WithDefault("false")
    boolean sealed();

    /**
     * Get the flag indicating if the stream allows rollup.
     *
     * @return the allows rollup flag
     */
    @WithDefault("false")
    boolean allowRollup();

    /**
     * Get the flag indicating if the stream allows direct message access.
     *
     * @return the allows direct flag
     */
    @WithDefault("false")
    boolean allowDirect();

    /**
     * Get the flag indicating if the stream allows
     * higher performance and unified direct access for mirrors as well.
     *
     * @return the allows direct flag
     */
    @WithDefault("false")
    boolean mirrorDirect();

    /**
     * Get the flag indicating if deny delete is set for the stream
     *
     * @return the deny delete flag
     */
    @WithDefault("false")
    boolean denyDelete();

    /**
     * Get the flag indicating if deny purge is set for the stream
     *
     * @return the deny purge flag
     */
    @WithDefault("false")
    boolean denyPurge();

    /**
     * Whether discard policy with max message per subject is applied per subject.
     *
     * @return the discard new per subject flag
     */
    @WithDefault("false")
    boolean discardNewPerSubject();

    /**
     * Metadata for the stream
     *
     * @return the metadata map. Might be null.
     */
    @NonNull
    Map<String, String> metadata();

    /**
     * The first sequence used in the stream.
     *
     * @return the first sequence
     */
    @WithDefault("1")
    long firstSequence();

    /**
     * Get the Subject Delete Marker TTL duration. May be null.
     *
     * @return The duration
     */
    @NonNull
    Optional<Duration> subjectDeleteMarkerTtl();

    /**
     * Whether Allow Message TTL is set
     *
     * @return the flag
     */
    @WithDefault("false")
    boolean allowMessageTtl();

    /**
     * Whether Allow Message Schedules is set
     *
     * @return the flag
     */
    @WithDefault("false")
    boolean allowMessageSchedules();

    /**
     * Whether Allow Message Counter is set
     *
     * @return the flag
     */
    @WithDefault("false")
    boolean allowMessageCounter();

    /**
     * Whether Allow Atomic Publish is set
     *
     * @return the flag
     */
    @WithDefault("false")
    boolean allowAtomicPublish();

    /**
     * Whether Allow Batched is set
     *
     * @return the flag
     */
    @WithDefault("false")
    boolean allowBatched();

    /**
     * Gets the persist mode or null if it was not explicitly set when creating or the server did not send it with stream info
     *
     * @return the persist mode
     */
    @WithDefault("Default")
    @NonNull
    Optional<PersistMode> persistMode();
}
