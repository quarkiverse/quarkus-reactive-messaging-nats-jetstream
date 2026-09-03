package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents a message structure that stores metadata and content typically used
 * within a NATS messaging system. This interface provides methods for accessing
 * various attributes of the message, such as its subject, payload, and status.
 */
public interface Message {

    /**
     * Retrieves the subject associated with the message.
     * The subject represents the NATS subject to which the message belongs.
     * If the subject is not defined, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the subject if present, or an empty {@code Optional} otherwise
     */
    @NonNull
    Optional<String> subject();

    /**
     * Retrieves the sequence number associated with the message.
     * The sequence number typically represents the position of the message
     * within its associated stream.
     *
     * @return the sequence number of the message
     */
    long sequence();

    /**
     * Retrieves the payload of the message, if available.
     * The payload typically represents the raw data content of the message.
     * If the payload is not set, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the payload as a byte array if present, or an empty {@code Optional} otherwise
     */
    @NonNull
    Optional<byte[]> payload();

    /**
     * Retrieves the timestamp associated with the message.
     * The timestamp typically represents the time at which the message
     * was published or processed, encoded as a {@link ZonedDateTime}.
     * If the timestamp is not available, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the timestamp if present,
     *         or an empty {@code Optional} otherwise
     */
    @NonNull
    Optional<ZonedDateTime> timestamp();

    /**
     * Retrieves the headers associated with the message.
     * The headers typically contain additional metadata about the message,
     * such as custom attributes or system-defined details.
     *
     * @return a {@code Headers} object representing the key-value pairs
     *         associated with the message's headers
     */
    @NonNull
    Headers headers();

    /**
     * Retrieves the stream name associated with the message.
     * The stream name typically represents the NATS JetStream stream
     * to which the message belongs.
     * If the stream name is not defined, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the stream name if present,
     *         or an empty {@code Optional} otherwise
     */
    @NonNull
    Optional<String> stream();

    /**
     * Retrieves the last sequence number associated with the message.
     * The last sequence number typically represents the highest sequence value
     * encountered within the associated stream or subscription.
     *
     * @return the last sequence number of the message
     */
    long lastSequence();

    /**
     * Retrieves the total number of pending messages associated with the current context.
     * This value typically represents messages waiting to be processed or acknowledged.
     *
     * @return the total number of pending messages
     */
    long numberOfPendingMessages();

    /**
     * Retrieves the status associated with the message.
     * The status provides additional information about the message's state,
     * such as whether it encountered an error or contains specific status details.
     * If no status is available, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the {@code Status} instance if present,
     *         or an empty {@code Optional} if no status information is available
     */
    @NonNull
    Optional<Status> status();

    static Message of(io.nats.client.api.MessageInfo messageInfo) {
        return MessageImpl.builder()
                .subject(Optional.ofNullable(messageInfo.getSubject()))
                .sequence(messageInfo.getSeq())
                .payload(Optional.ofNullable(messageInfo.getData()))
                .timestamp(Optional.ofNullable(messageInfo.getTime()))
                .headers(messageInfo.getHeaders() != null ? Headers.of(messageInfo.getHeaders()) : Headers.of())
                .stream(Optional.ofNullable(messageInfo.getStream()))
                .lastSequence(messageInfo.getLastSeq())
                .numberOfPendingMessages(messageInfo.getNumPending())
                .status(Status.of(messageInfo))
                .build();
    }
}
