package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents the status of a message.
 * This interface defines methods to retrieve details about
 * the status, such as a descriptive message, a numeric code,
 * and whether it represents an error condition.
 */
public interface Status {

    /**
     * Creates an {@code Optional} containing a {@code Status} instance based on the
     * provided {@code MessageInfo}. The method extracts the status information from
     * {@code MessageInfo} and constructs a {@code StatusRecord} if the status is available.
     * If no status is present, an empty {@code Optional} is returned.
     *
     * @param messageInfo the {@code MessageInfo} instance from which status information
     *        is retrieved; must not be null
     * @return an {@code Optional} containing a {@code Status} instance if status information
     *         is available, or an empty {@code Optional} if no status is present
     */
    static Optional<Status> of(io.nats.client.api.MessageInfo messageInfo) {
        final var status = messageInfo.getStatus();
        if (status != null) {
            return Optional.of(StatusImpl.builder()
                    .message(Optional.ofNullable(status.getMessage()))
                    .code(status.getCode())
                    .error(messageInfo.isErrorStatus())
                    .build());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the message associated with the current status.
     * The message is typically a descriptive representation of the
     * status, such as an error message or informational text.
     *
     * @return a string representing the message associated with the status
     */
    @NonNull
    Optional<String> message();

    /**
     * Retrieves the numeric code associated with the current status.
     * The code is typically used to represent the status in a machine-readable format,
     * such as an error code or an identifier for the status type.
     *
     * @return an integer representing the code associated with the status
     */
    int code();

    /**
     * Indicates whether the current status represents an error condition.
     * This method is typically used to verify if the status signifies
     * a failure or erroneous state.
     *
     * @return {@code true} if the current status represents an error;
     *         {@code false} otherwise
     */
    boolean error();
}
