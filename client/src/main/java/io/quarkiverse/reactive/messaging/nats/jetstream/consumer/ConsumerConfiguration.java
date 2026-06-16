package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

public interface ConsumerConfiguration {

    /**
     * The consumer name
     */
    String name();

    /**
     * The stream name
     */
    String stream();

    /**
     * Set to true if the consumer should be durable.
     */
    Boolean durable();

    /**
     * A list of subjects that overlap with the subjects bound to the stream to filter delivery to subscribers
     */
    List<String> filterSubjects();

    /**
     * The duration that the server will wait for an ack for any individual message once it has been delivered to a consumer.
     * If an ack is not received in time, the message will be re-delivered.
     */
    Optional<Duration> acknowledgeWait();

    /**
     * The point in the stream to receive messages from, either DeliverAll, DeliverLast, DeliverNew, DeliverByStartSequence,
     * DeliverByStartTime, or DeliverLastPerSubject
     */
    DeliverPolicy deliverPolicy();

    /**
     * The start sequence
     */
    Optional<Long> startSequence();

    /**
     * The start time
     */
    Optional<ZonedDateTime> startTime();

    /**
     * A description of the consumer
     */
    Optional<String> description();

    /**
     * Duration that instructs the server to clean up consumers that are inactive for that long
     */
    Optional<Duration> inactiveThreshold();

    /**
     * Defines the maximum number of messages, without an acknowledgement, that can be outstanding
     */
    Optional<Long> maxAckPending();

    /**
     * The maximum number of times a specific message delivery will be attempted
     */
    Optional<Long> maxDeliver();

    /**
     * If the policy is ReplayOriginal, the messages in the stream will be pushed to the client at the same rate that
     * they were originally received, simulating the original timing of messages. If the policy is
     * ReplayInstant (the default), the messages will be pushed to the client as fast as possible while adhering to the
     * Ack Policy, Max Ack Pending, and the client's ability to consume those messages
     */
    ReplayPolicy replayPolicy();

    /**
     * Sets the number of replicas for the consumer's state. By default, when the value is set to zero, consumers
     * inherit the number of replicas from the stream
     */
    Optional<Integer> replicas();

    /**
     * If set, forces the consumer state to be kept in memory rather than inherit the storage type of the
     * stream (file in this case)
     */
    Optional<Boolean> memoryStorage();

    /**
     * The sample frequency
     */
    Optional<String> sampleFrequency();

    /**
     * The consumer metadata
     */
    Map<String, String> metadata();

    /**
     * The timing of re-deliveries as a list of durations
     */
    List<Duration> backoff();

    /**
     * The pause until
     */
    Optional<ZonedDateTime> pauseUntil();

    /**
     * The duration to wait for an ack confirmation
     */
    Duration acknowledgeTimeout();

    /**
     * Retrieves an optional configuration for pull-based consumers.
     * This configuration specifies parameters such as the maximum expiry
     * time for messages and the maximum number of waiting pull requests.
     *
     * @return an {@code Optional} containing the {@code PullConfiguration}
     *         if pull-based configuration is enabled; otherwise, an empty {@code Optional}.
     */
    Optional<PullConfiguration> pull();
}
