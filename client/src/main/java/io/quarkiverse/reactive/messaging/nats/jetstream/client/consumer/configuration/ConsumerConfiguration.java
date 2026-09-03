package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.smallrye.config.WithDefault;

/**
 * Interface representing the consumerConfiguration for a consumer in NATS.
 * This consumerConfiguration defines various parameters that dictate the behavior of the consumer,
 * including message delivery policies, acknowledgment settings, and more.
 */
public interface ConsumerConfiguration {

    /**
     * Maps an existing NATS ConsumerConfiguration to a new instance of ConsumerConfiguration.
     *
     * @param source the source {@code io.nats.client.api.ConsumerConfiguration} object to map from
     * @return a new instance of {@code ConsumerConfiguration} mapped from the provided source
     */
    static @NonNull ConsumerConfiguration of(io.nats.client.api.ConsumerConfiguration source) {
        final var mapper = Mappers.getMapper(ConsumerConfigurationMapper.class);
        return mapper.map(source);
    }

    /**
     * Creates a new {@code io.nats.client.api.ConsumerConfiguration} from an existing
     * {@code ConsumerConfiguration} instance.
     *
     * @param configuration the non-null source {@code ConsumerConfiguration} object to map from
     * @return a new non-null instance of {@code io.nats.client.api.ConsumerConfiguration} mapped
     *         from the provided source
     */
    static io.nats.client.api.@NonNull ConsumerConfiguration of(@NonNull final ConsumerConfiguration configuration) {
        final var mapper = Mappers.getMapper(ConsumerConfigurationMapper.class);
        return mapper.map(configuration);
    }

    /**
     * Retrieves the name of the consumer.
     *
     * @return the non-null name of the consumer
     */
    @NonNull
    String name();

    /*
     * If set, clients can have subscriptions bind to the consumer and resume until the consumer is explicitly deleted.
     *
     * @return the durable flag
     */
    @WithDefault("false")
    boolean durable();

    /*
     * A subject that overlaps with the subjects bound to the stream to filter delivery to subscribers.
     * Note: This cannot be used with the filterSubjects field.
     *
     * @return the filter subject
     */
    @NonNull
    Optional<String> filterSubject();

    /*
     * A set of subjects that overlap with the subjects bound to the stream to filter delivery to subscribers.
     * Note: This cannot be used with the filterSubject field.
     *
     * @return the filter subjects
     */
    @NonNull
    Optional<Set<String>> filterSubjects();

    /*
     * The duration that the server will wait for an acknowledge for any individual message once it has been delivered to a
     * consumer.
     * If an ack is not received in time; the message will be re-delivered.
     *
     * @return the acknowledge wait
     */
    @NonNull
    Optional<Duration> acknowledgeWait();

    /*
     * The duration to wait for an acknowledge confirmation
     *
     * @return the acknowledge timeout
     */
    @WithDefault("10s")
    @NonNull
    Duration acknowledgeTimeout();

    /*
     * The point in the stream to receive messages from; either DeliverAll; DeliverLast; DeliverNew; DeliverByStartSequence;
     * DeliverByStartTime; or DeliverLastPerSubject
     *
     * @return the deliver policy
     */
    @WithDefault("All")
    @NonNull
    DeliverPolicy deliverPolicy();

    /*
     * Used with the DeliverByStartSequence deliver policy.
     *
     * @return the start sequence
     */
    @WithDefault("0")
    long startSequence();

    /*
     * Used with the DeliverByStartTime deliver policy.
     *
     * @return the start time
     */
    @NonNull
    Optional<ZonedDateTime> startTime();

    /*
     * A description of the consumer. This can be particularly useful for ephemeral consumers to indicate their
     * purpose since a durable name cannot be provided.
     *
     * @return the description
     */
    @NonNull
    Optional<String> description();

    /*
     * Duration that instructs the server to clean up consumers inactive for that long. Prior to 2.9; this only applied
     * to ephemeral consumers.
     *
     * @return the inactive threshold
     */
    @NonNull
    Optional<Duration> inactiveThreshold();

    /*
     * Defines the maximum number of messages; without acknowledgment; that can be outstanding. Once this limit is
     * reached; message delivery will be suspended. This limit applies across all of the consumer's bound subscriptions.
     * A value of -1 means there can be any number of pending acknowledgments (i.e.; no flow control). The default is 1000.
     *
     * @return the maximum number of pending acknowledgments
     */
    @NonNull
    Optional<Long> maxAcknowledgePending();

    /*
     * The maximum number of times a specific message delivery will be attempted. Applies to any message that is re-sent
     * due to acknowledgment policy (i.e.; due to a negative acknowledgment or no acknowledgment sent by the client).
     * The default is -1 (redeliver until acknowledged). Messages that have reached the maximum delivery count will
     * stay in the stream.
     *
     * @return the maximum number of deliveries
     */
    @NonNull
    Optional<Long> maxDeliver();

    /*
     * A sequence of delays controlling the re-delivery of messages on acknowledgment timeout (but not on nak).
     * The sequence length must be less than or equal to MaxDeliver. If backoff is not set; a timeout will result in
     * immediate re-delivery. E.g.; MaxDeliver=5 backoff=[5s; 30s; 300s; 3600s; 84000s] will re-deliver a message 5
     * times over one day. When MaxDeliver is larger than the backoff list; the last delay in the list will apply for
     * the remaining deliveries. Note that backoff is NOT applied to naked messages. A nak will result in immediate
     * re-delivery unless nakWithDelay is used to set the re-delivery delay explicitly. When BackOff is set; it
     * overrides AckWait entirely. The first value in the BackOff determines the AckWait value.
     *
     * @return the backoff
     */
    @NonNull
    Optional<List<Duration>> backoff();

    /*
     * If the policy is ReplayOriginal; the messages in the stream will be pushed to the client at the same rate they
     * were originally received; simulating the original timing. If the policy is ReplayInstant (default);
     * the messages will be pushed to the client as fast as possible while adhering to the acknowledgment policy;
     * Max Ack Pending; and the client's ability to consume those messages.
     *
     * @return the replay policy
     */
    @WithDefault("Instant")
    @NonNull
    ReplayPolicy replayPolicy();

    /*
     * Sets the number of replicas for the consumer's state. By default; when the value is set to Optional.empty(); consumers
     * inherit the number of replicas from the stream.
     *
     * @return the number of replicas
     */
    @NonNull
    Optional<Integer> replicas();

    /*
     * If set; forces the consumer state to be kept in memory rather than inherit the storage type of the stream
     * (default is file storage). This reduces I/O from acknowledgments; useful for ephemeral consumers.
     *
     * @return whether the consumer state should be kept in memory
     */
    @WithDefault("false")
    boolean memoryStorage();

    /*
     * Sets the percentage of acknowledgments that should be sampled for observability; 0-100.
     * This value is a string and allows both 30 and 30% as valid values.
     *
     * @return the sample frequency
     */
    @NonNull
    Optional<String> sampleFrequency();

    /*
     * A set of application-defined key-value pairs for associating metadata with the consumer.
     *
     * @return the metadata
     */
    @NonNull
    Map<String, String> metadata();

    /*
     * Delivers only the headers of messages in the stream; adding a Nats-Msg-Size header indicating the size
     * of the removed payload.
     *
     * @return whether only headers should be delivered
     */
    @WithDefault("false")
    boolean headersOnly();

    /*
     * The time until the consumer is paused
     *
     * @return the pause until time
     */
    @NonNull
    Optional<ZonedDateTime> pauseUntil();

    /**
     * Retrieves the pull options configured for the consumer.
     * Provides an {@link Optional} of {@link PullOptions} that defines
     * pull-based consumption parameters such as maximum batch size,
     * maximum byte limits, and request timeouts.
     *
     * @return an {@code Optional<PullOptions>} containing the pull options if configured,
     *         or an empty {@code Optional} if no pull options are set.
     */
    @NonNull
    Optional<PullOptions> pullOptions();

    /**
     * Retrieves the push options configured for the consumer.
     * Provides an {@link Optional} of {@link PushOptions} that defines
     * push-based consumption parameters such as maximum inflight messages,
     * flow control, and delivery acknowledgment settings.
     *
     * @return an {@code Optional<PushOptions>} containing the push options if configured,
     *         or an empty {@code Optional} if no push options are set.
     */
    @NonNull
    Optional<PushOptions> pushOptions();
}
