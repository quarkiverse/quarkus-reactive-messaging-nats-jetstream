package io.quarkiverse.reactive.nats.jetstream;

import io.quarkiverse.reactive.nats.jetstream.connection.ConnectionConfiguration;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public interface ClientConfiguration {

    ConnectionConfiguration connection();

    /**
     * Retrieves an optional {@code Duration} that specifies the timeout for acknowledging messages.
     * The timeout determines the maximum time allowed for message acknowledgment.
     *
     * @return an {@code Optional} containing the {@code Duration} of the acknowledgment timeout if configured,
     *         or an empty {@code Optional} if no timeout is specified.
     */
    @NonNull Optional<Duration> acknowledgeTimeout();

    /**
     * Backoff strategy for handling message acknowledgment retries.
     */
    @NonNull List<Duration> backoff();

}
