package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.HashMap;
import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * A specialized implementation of {@link HashMap} that represents headers for publishing messages.
 * This class extends {@link HashMap} with a key of type {@link String} and a value of type {@link List}<{@link String}>.
 * It also implements the {@link Headers} interface, serving as a container for managing header data.
 * This class provides utility methods to create empty or pre-initialized {@code PublishHeaders} instances.
 */
public class PublishHeaders extends HashMap<String, List<String>> implements Headers {

    /**
     * Creates a new, empty instance of {@code PublishHeaders}.
     *
     * @return A new {@link PublishHeaders} instance with no initial headers.
     */
    public static @NonNull PublishHeaders of() {
        return new PublishHeaders();
    }

    /**
     * Creates a new instance of {@code PublishHeaders} with the specified message ID.
     * The specified message ID is associated with the "Nats-Msg-Id" key in the headers.
     * This allows uniquely identifying messages in a NATS JetStream context.
     *
     * @param messageId the unique message identifier to be set in the headers
     * @return A new {@link PublishHeaders} instance with the specified message ID set
     */
    public static @NonNull PublishHeaders of(String messageId) {
        final var headers = new PublishHeaders();
        headers.setMessageId(messageId);
        return headers;
    }

    PublishHeaders() {
        super();
    }
}
