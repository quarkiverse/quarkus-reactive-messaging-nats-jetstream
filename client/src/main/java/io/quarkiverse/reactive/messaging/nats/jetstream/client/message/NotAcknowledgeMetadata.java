package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the metadata for a message that has been marked as not acknowledged (NAK) in JetStream.
 * This interface extends {@code Metadata} and provides additional behavior specific to NAK messages.
 * A NAK message indicates that the message was received but requires re-delivery for further processing.
 */
public interface NotAcknowledgeMetadata extends Metadata {

    static @NonNull NotAcknowledgeMetadata of(@NonNull Duration delay) {
        return new NotAcknowledgeMetadataImpl(Optional.of(delay));
    }

    /**
     * Not acknowledges a JetStream message has been received but indicates that the message is not completely processed and
     * should be sent again later.
     *
     * @return an {@code Optional} containing a {@code Duration} if a delay
     *         is specified, or an empty {@code Optional} if no delay is set
     */
    @NonNull
    Optional<Duration> withDelay();

}
