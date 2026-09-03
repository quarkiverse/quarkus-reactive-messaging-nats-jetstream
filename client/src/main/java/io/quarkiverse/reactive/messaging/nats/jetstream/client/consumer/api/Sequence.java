package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the sequence numbers for a consumer
 */
public interface Sequence {

    /**
     * The consumer sequence number.
     */
    long consumerSequence();

    /**
     * The stream sequence number
     */
    long streamSequence();

    /**
     * The last time a message was delivered or acknowledged
     */
    @NonNull
    Optional<ZonedDateTime> lastActive();
}
