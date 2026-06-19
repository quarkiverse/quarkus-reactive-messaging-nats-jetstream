package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import lombok.Builder;
import lombok.NonNull;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

@Builder
public record ConsumerConfiguration(
        /*
         * The consumer name
         */
        @NonNull String name,
        /*
         * If set, clients can have subscriptions bind to the consumer and resume until the consumer is explicitly deleted.
         */
        @NonNull Boolean durable,
        /*
         * A subject that overlaps with the subjects bound to the stream to filter delivery to subscribers.
         * Note: This cannot be used with the filterSubjects field.
         */
        @NonNull Optional<String> filterSubject,
        /*
         * A set of subjects that overlap with the subjects bound to the stream to filter delivery to subscribers.
         * Note: This cannot be used with the filterSubject field.
         */
        @NonNull Set<String> filterSubjects,
        /*
         * The duration that the server will wait for an ack for any individual message once it has been delivered to a consumer.
         * If an ack is not received in time, the message will be re-delivered.
         */
        @NonNull Optional<Duration> acknowledgeWait,
        /*
         * The point in the stream to receive messages from, either DeliverAll, DeliverLast, DeliverNew, DeliverByStartSequence,
         * DeliverByStartTime, or DeliverLastPerSubject
         */
        @NonNull DeliverPolicy deliverPolicy,
        /*
         * Used with the DeliverByStartSequence deliver policy.
         */
        @NonNull Optional<Long> startSequence,
        /*
         * Used with the DeliverByStartTime deliver policy.
         */
        @NonNull Optional<ZonedDateTime> startTime,
        /*
         * A description of the consumer. This can be particularly useful for ephemeral consumers to indicate their
         * purpose since a durable name cannot be provided.
         */
        @NonNull Optional<String> description,
        /*
         * Duration that instructs the server to clean up consumers inactive for that long. Prior to 2.9, this only applied
         * to ephemeral consumers.
         */
        @NonNull Optional<Duration> inactiveThreshold,
        /*
         * Defines the maximum number of messages, without acknowledgment, that can be outstanding. Once this limit is
         * reached, message delivery will be suspended. This limit applies across all of the consumer's bound subscriptions.
         * A value of -1 means there can be any number of pending acknowledgments (i.e., no flow control). The default is 1000.
         */
        @NonNull Optional<Long> maxAcknowledgePending,
        /*
         * The maximum number of times a specific message delivery will be attempted. Applies to any message that is re-sent
         * due to acknowledgment policy (i.e., due to a negative acknowledgment or no acknowledgment sent by the client).
         * The default is -1 (redeliver until acknowledged). Messages that have reached the maximum delivery count will
         * stay in the stream.
         */
        @NonNull Optional<Long> maxDeliver,
        /*
         * A sequence of delays controlling the re-delivery of messages on acknowledgment timeout (but not on nak).
         * The sequence length must be less than or equal to MaxDeliver. If backoff is not set, a timeout will result in
         * immediate re-delivery. E.g., MaxDeliver=5 backoff=[5s, 30s, 300s, 3600s, 84000s] will re-deliver a message 5
         * times over one day. When MaxDeliver is larger than the backoff list, the last delay in the list will apply for
         * the remaining deliveries. Note that backoff is NOT applied to naked messages. A nak will result in immediate
         * re-delivery unless nakWithDelay is used to set the re-delivery delay explicitly. When BackOff is set, it
         * overrides AckWait entirely. The first value in the BackOff determines the AckWait value.
         */
        @NonNull List<Duration> backoff,
        /*
         * If the policy is ReplayOriginal, the messages in the stream will be pushed to the client at the same rate they
         * were originally received, simulating the original timing. If the policy is ReplayInstant (default),
         * the messages will be pushed to the client as fast as possible while adhering to the acknowledgment policy,
         * Max Ack Pending, and the client's ability to consume those messages.
         */
        @NonNull ReplayPolicy replayPolicy,
        /*
         * Sets the number of replicas for the consumer's state. By default, when the value is set to zero, consumers
         * inherit the number of replicas from the stream.
         */
        @NonNull Optional<Integer> replicas,
        /*
         * If set, forces the consumer state to be kept in memory rather than inherit the storage type of the stream
         * (default is file storage). This reduces I/O from acknowledgments, useful for ephemeral consumers.
         */
        @NonNull Optional<Boolean> memoryStorage,
        /*
         * Sets the percentage of acknowledgments that should be sampled for observability, 0-100.
         * This value is a string and allows both 30 and 30% as valid values.
         */
        @NonNull Optional<String> sampleFrequency,
        /*
         * A set of application-defined key-value pairs for associating metadata with the consumer.
         */
        @NonNull Map<String, String> metadata,
        /*
         * Delivers only the headers of messages in the stream, adding a Nats-Msg-Size header indicating the size
         * of the removed payload.
         */
        @NonNull Optional<Boolean> headersOnly,
        /*
         * Retrieves an optional configuration for pull-based consumers.
         * This configuration specifies parameters such as the maximum expiry
         * time for messages and the maximum number of waiting pull requests.
         */
        @NonNull Optional<PullConfiguration> pullConfiguration) {

    public ConsumerConfiguration {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(durable, "durable");
        Objects.requireNonNull(filterSubject, "filterSubject");
        Objects.requireNonNull(filterSubjects, "filterSubjects");
        Objects.requireNonNull(acknowledgeWait, "acknowledgeWait");
        Objects.requireNonNull(deliverPolicy, "deliverPolicy");
        Objects.requireNonNull(startSequence, "startSequence");
        Objects.requireNonNull(startTime, "startTime");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(inactiveThreshold, "inactiveThreshold");
        Objects.requireNonNull(maxAcknowledgePending, "maxAcknowledgePending");
        Objects.requireNonNull(maxDeliver, "maxDeliver");
        Objects.requireNonNull(backoff, "backoff");
        Objects.requireNonNull(replayPolicy, "replayPolicy");
        Objects.requireNonNull(replicas);
        Objects.requireNonNull(memoryStorage);
        Objects.requireNonNull(sampleFrequency);
        Objects.requireNonNull(metadata);
        Objects.requireNonNull(headersOnly);
        Objects.requireNonNull(pullConfiguration);
    }

}
