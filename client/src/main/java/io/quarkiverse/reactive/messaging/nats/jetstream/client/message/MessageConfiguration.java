package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Optional;

public interface MessageConfiguration extends Metadata {

    /**
     * Retrieves an optional {@code Duration} that specifies the timeout for acknowledging messages.
     * The timeout determines the maximum time allowed for message acknowledgment.
     *
     * @return an {@code Optional} containing the {@code Duration} of the acknowledgment timeout if configured,
     *         or an empty {@code Optional} if no timeout is specified.
     */
    @NonNull
    Optional<Duration> acknowledgeTimeout();

}
