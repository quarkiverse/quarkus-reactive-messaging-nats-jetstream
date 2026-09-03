package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.HashMap;
import java.util.List;

import org.jspecify.annotations.NonNull;

/**
 * Represents a collection of message headers, extending the functionality of a
 * HashMap where each key is a header name and the corresponding value is a list
 * of string values associated with that header.
 * This class provides a concrete implementation for managing message headers,
 * allowing headers to be stored, retrieved, and manipulated in a structured manner.
 * It implements the Headers interface and inherits behavior from HashMap.
 * The MessageHeaders class also includes a static factory method for creating an
 * instance of MessageHeaders based on a NativeMessage object.
 */
public class MessageHeaders extends HashMap<String, List<String>> implements Headers {

    /**
     * Creates a new instance of {@code MessageHeaders} based on the headers present in the
     * provided {@code NativeMessage}.
     * If the given {@code NativeMessage} contains headers, they are transferred to the
     * newly created {@code MessageHeaders} instance.
     *
     * @param message the {@code NativeMessage} from which headers are extracted;
     *        must not be null.
     * @return a new {@code MessageHeaders} instance containing headers from the specified
     *         {@code NativeMessage}. If the message has no headers, an empty
     *         {@code MessageHeaders} is returned.
     */
    static @NonNull MessageHeaders of(@NonNull NativeMessage message) {
        final var result = new MessageHeaders();
        if (message.hasHeaders()) {
            message.getHeaders().entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    MessageHeaders() {
        super();
    }
}
