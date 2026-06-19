package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

@Builder
public record MessageConfiguration(
        /*
         * Retrieves an optional {@code Duration} that specifies the timeout for acknowledging messages.
         * The timeout determines the maximum time allowed for message acknowledgment.
         *
         * @return an {@code Optional} containing the {@code Duration} of the acknowledgment timeout if configured,
         *         or an empty {@code Optional} if no timeout is specified.
         */
        @NonNull
        Optional<Duration> acknowledgeTimeout) implements Metadata {

    public MessageConfiguration {
        Objects.requireNonNull(acknowledgeTimeout, "acknowledgeTimeout");
    }
}
