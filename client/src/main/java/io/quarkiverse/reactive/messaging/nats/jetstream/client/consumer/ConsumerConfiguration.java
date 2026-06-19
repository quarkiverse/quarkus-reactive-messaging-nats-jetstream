package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import lombok.Builder;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Builder
public record ConsumerConfiguration(String name,
                                    Boolean durable,
                                    Optional<String> filterSubject,
                                    Set<String> filterSubjects,
                                    Optional<Duration> acknowledgeWait,
                                    DeliverPolicy deliverPolicy,
                                    Optional<Long> startSequence,
                                    Optional<ZonedDateTime> startTime,
                                    Optional<String> description,
                                    Optional<Duration> inactiveThreshold,
                                    Optional<Long> maxAcknowledgePending,
                                    Optional<Long> maxDeliver,
                                    ReplayPolicy replayPolicy,
                                    Optional<Integer> replicas,
                                    Optional<Boolean> memoryStorage,
                                    Optional<String> sampleFrequency,
                                    Map<String, String> metadata,
                                    List<Duration> backoff,
                                    Optional<ZonedDateTime> pauseUntil,
                                    Duration acknowledgeTimeout,
                                    Optional<Boolean> headersOnly,
                                    PullConfiguration pullConfiguration) {


    /**
     * The consumer name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * If set, clients can have subscriptions bind to the consumer and resume until the consumer is explicitly deleted.
     * A durable name cannot contain whitespace, ., *, >, path separators (forward or backward slash), or non-printable characters.
     */
    @Override
    public Boolean durable() {
        return durable;
    }

    /**
     * A subject that overlaps with the subjects bound to the stream to filter delivery to subscribers.
     * Note: This cannot be used with the filterSubjects field.
     */
    @Override
    public Optional<String> filterSubject() {
        return filterSubject;
    }

    /**
     * A set of subjects that overlap with the subjects bound to the stream to filter delivery to subscribers.
     * Note: This cannot be used with the filterSubject field.
     */
    @Override
    public Set<String> filterSubjects() {
        return filterSubjects;
    }

    /**
     * The duration that the server will wait for an ack for any individual message once it has been delivered to a consumer.
     * If an ack is not received in time, the message will be re-delivered.
     */
    @Override
    public Optional<Duration> acknowledgeWait() {
        return acknowledgeWait;
    }

    /**
     * The point in the stream to receive messages from, either DeliverAll, DeliverLast, DeliverNew, DeliverByStartSequence,
     * DeliverByStartTime, or DeliverLastPerSubject
     */
    @Override
    public DeliverPolicy deliverPolicy() {
        return deliverPolicy;
    }

    /**
     * Used with the DeliverByStartSequence deliver policy.
     */
    public Optional<Long> startSequence() {
        return startSequence;
    }

    /**
     * Used with the DeliverByStartTime deliver policy.
     */
    @Override
    public Optional<ZonedDateTime> startTime() {
        return startTime;
    }

    /**
     * A description of the consumer. This can be particularly useful for ephemeral consumers to indicate their
     * purpose since a durable name cannot be provided.
     */
    @Override
    public Optional<String> description() {
        return description;
    }

    /**
     * Duration that instructs the server to clean up consumers inactive for that long. Prior to 2.9, this only applied
     * to ephemeral consumers.
     */
    @Override
    public Optional<Duration> inactiveThreshold() {
        return inactiveThreshold;
    }

    /**
     * Defines the maximum number of messages, without acknowledgment, that can be outstanding. Once this limit is
     * reached, message delivery will be suspended. This limit applies across all of the consumer's bound subscriptions.
     * A value of -1 means there can be any number of pending acknowledgments (i.e., no flow control). The default is 1000.
     */
    @Override
    public Optional<Long> maxAcknowledgePending() {
        return maxAcknowledgePending;
    }

    /**
     * The maximum number of times a specific message delivery will be attempted. Applies to any message that is re-sent
     * due to acknowledgment policy (i.e., due to a negative acknowledgment or no acknowledgment sent by the client).
     * The default is -1 (redeliver until acknowledged). Messages that have reached the maximum delivery count will
     * stay in the stream.
     */
    @Override
    public Optional<Long> maxDeliver() {
        return maxDeliver;
    }


    /**
     * A sequence of delays controlling the re-delivery of messages on acknowledgment timeout (but not on nak).
     * The sequence length must be less than or equal to MaxDeliver. If backoff is not set, a timeout will result in
     * immediate re-delivery. E.g., MaxDeliver=5 backoff=[5s, 30s, 300s, 3600s, 84000s] will re-deliver a message 5
     * times over one day. When MaxDeliver is larger than the backoff list, the last delay in the list will apply for
     * the remaining deliveries. Note that backoff is NOT applied to naked messages. A nak will result in immediate
     * re-delivery unless nakWithDelay is used to set the re-delivery delay explicitly. When BackOff is set, it
     * overrides AckWait entirely. The first value in the BackOff determines the AckWait value.
     */
    @Override
    public List<Duration> backoff() {
        return backoff;
    }

    /**
     * If the policy is ReplayOriginal, the messages in the stream will be pushed to the client at the same rate they
     * were originally received, simulating the original timing. If the policy is ReplayInstant (default),
     * the messages will be pushed to the client as fast as possible while adhering to the acknowledgment policy,
     * Max Ack Pending, and the client's ability to consume those messages.
     */
    @Override
    public ReplayPolicy replayPolicy() {
        return replayPolicy;
    }

    /**
     * Sets the number of replicas for the consumer's state. By default, when the value is set to zero, consumers
     * inherit the number of replicas from the stream.
     */
    @Override
    public Optional<Integer> replicas() {
        return replicas;
    }

    /**
     * If set, forces the consumer state to be kept in memory rather than inherit the storage type of the stream
     * (default is file storage). This reduces I/O from acknowledgments, useful for ephemeral consumers.
     */
    @Override
    public Optional<Boolean> memoryStorage() {
        return memoryStorage;
    }

    /**
     * Sets the percentage of acknowledgments that should be sampled for observability, 0-100.
     * This value is a string and allows both 30 and 30% as valid values.
     */
    @Override
    public Optional<String> sampleFrequency() {
        return sampleFrequency;
    }

    /**
     * A set of application-defined key-value pairs for associating metadata with the consumer.
     */
    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * Delivers only the headers of messages in the stream, adding a Nats-Msg-Size header indicating the size
     * of the removed payload.
     */
    @Override
    public Optional<Boolean> headersOnly() {
        return headersOnly;
    }

    /**
     * Retrieves an optional configuration for pull-based consumers.
     * This configuration specifies parameters such as the maximum expiry
     * time for messages and the maximum number of waiting pull requests.
     *
     * @return an {@code Optional} containing the {@code PullConfiguration}
     *         if pull-based configuration is enabled; otherwise, an empty {@code Optional}.
     */
    @Override
    public PullConfiguration pullConfiguration() {
        return pullConfiguration;
    }
}
