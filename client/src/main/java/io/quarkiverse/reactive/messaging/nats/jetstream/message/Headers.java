package io.quarkiverse.reactive.messaging.nats.jetstream.message;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * The {@code Headers} interface extends both {@code Map<String, List<String>>}
 * and {@code Metadata}, providing a specialized structure for managing
 * message headers in a reactive messaging context. It includes utility methods
 * for interacting with NATS-specific headers and payload metadata.
 * This interface defines various default methods to facilitate the management
 * of headers, such as determining payload types, setting message types, and
 * accessing message IDs in a standardized way.
 */
public final class Headers extends HashMap<String, List<String>> implements Metadata {
    private final static String MESSAGE_TYPE_HEADER = "message.type";
    private final static String MESSAGE_SUBJECT_HEADER = "message.subject";
    private final static String MESSAGE_STREAM_HEADER = "message.stream";

    static @NonNull Headers of() {
        return new Headers();
    }

    static @NonNull Headers of(io.nats.client.impl.@NonNull Headers headers) {
        final var result = new Headers();
        headers.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    private Headers() {
        super();
    }

    /**
     * Converts the current Headers instance into a NATS-compatible Headers object.
     * This method creates a new instance of {@code io.nats.client.impl.Headers},
     * iterates over the current instance, and adds all key-value pairs from the current
     * headers to the resulting instance.
     *
     * @return a {@code io.nats.client.impl.Headers} object containing the entries
     *         from the current Headers instance.
     */
    public io.nats.client.impl.Headers to() {
        final var result = new io.nats.client.impl.Headers();
        forEach(result::put);
        return result;
    }

    /**
     * Attempts to determine the payload type from the current headers.
     * This method retrieves the value associated with the {@code MESSAGE_TYPE_HEADER} key
     * and attempts to load the corresponding class. If the header is not present
     * or cannot be resolved to a valid class, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the payload type as a {@code Class<T>} if
     *         present and successfully loaded, or an empty {@code Optional} otherwise
     */
    public <T> Optional<Class<T>> payloadType() {
        return Optional.ofNullable(get(MESSAGE_TYPE_HEADER)).map(List::getFirst).map(this::loadClass);
    }

    /**
     * Retrieves the NATS message ID associated with the headers.
     * The message ID is typically stored under the "Nats-Msg-Id" key in the headers.
     * If the key is not present or its value is null, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the message ID if present, or an empty {@code Optional} otherwise
     */
    public Optional<String> messageId() {
        return Optional.ofNullable(get("Nats-Msg-Id")).map(List::getFirst);
    }

    /**
     * Sets the NATS message ID in the headers.
     * This method associates the given message ID with the "Nats-Msg-Id" key
     * in the headers. The message ID can be used to uniquely identify messages
     * in a NATS JetStream context.
     *
     * @param messageId the unique message identifier to be set in the headers
     */
    public void setMessageId(String messageId) {
        put("Nats-Msg-Id", List.of(messageId));
    }

    public @NonNull Optional<String> stream() {
        return Optional.ofNullable(get(MESSAGE_STREAM_HEADER)).map(List::getFirst);
    }

    public void setStream(String stream) {
        put(MESSAGE_STREAM_HEADER, List.of(stream));
    }

    public @NonNull Optional<String> subject() {
        return Optional.ofNullable(get(MESSAGE_SUBJECT_HEADER)).map(List::getFirst);
    }

    public void setSubject(String subject) {
        put(MESSAGE_SUBJECT_HEADER, List.of(subject));
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return (Class<T>) classLoader.loadClass(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
