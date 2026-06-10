package io.quarkiverse.reactive.nats.jetstream.message;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface MessageConfiguration<T> extends Metadata {

    /**
     * Retrieves an optional {@code Duration} that specifies the timeout for acknowledging messages.
     * The timeout determines the maximum time allowed for message acknowledgment.
     *
     * @return an {@code Optional} containing the {@code Duration} of the acknowledgment timeout if configured,
     *         or an empty {@code Optional} if no timeout is specified.
     */
    @NonNull
    Optional<Duration> acknowledgeTimeout();

    /**
     * Backoff strategy for handling message acknowledgment retries.
     */
    @NonNull
    List<Duration> backoff();

    /**
     * Retrieves an instance of {@code PayloadMapper} associated with the message configuration.
     * The {@code PayloadMapper} is responsible for converting objects to byte arrays and
     * deserializing byte arrays back into objects, facilitating the handling of message payloads.
     *
     * @return an instance of {@code PayloadMapper} for transforming message payloads.
     */
    PayloadMapper<T> payloadMapper();
}
