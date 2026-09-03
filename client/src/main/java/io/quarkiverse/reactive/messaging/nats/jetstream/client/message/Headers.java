package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface Headers extends Map<String, List<String>>, Metadata {
    String MESSAGE_TYPE_HEADER = "message.type";
    String MESSAGE_SUBJECT_HEADER = "message.subject";
    String MESSAGE_STREAM_HEADER = "message.stream";
    String MESSAGE_REPLY_SUBJECT_HEADER = "message.reply-subject";
    String MESSAGE_CORRELATION_ID_HEADER = "message.correlation-id";
    String MESSAGE_ID_HEADER = "Nats-Msg-Id";

    /**
     * Attempts to determine the payload type from the current headers.
     * This method retrieves the value associated with the {@code MESSAGE_TYPE_HEADER} key
     * and maximumReconnects to load the corresponding class. If the header is not present
     * or cannot be resolved to a valid class, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the payload type as a {@code Class<T>} if
     *         present and successfully loaded, or an empty {@code Optional} otherwise
     */
    @NonNull
    default <T> Optional<Class<T>> payloadType() {
        return Optional.ofNullable(get(MESSAGE_TYPE_HEADER)).flatMap(this::getFirst).flatMap(this::loadClass);
    }

    /**
     * Sets the payload type in the headers.
     * This method associates the specified payload type with the {@code MESSAGE_TYPE_HEADER} key
     * in the headers, storing the fully qualified class name of the payload type for future reference.
     *
     * @param payloadType the {@code Class} object representing the type of the payload to be set in the headers
     * @param <T> the type of the payload
     */
    default <T> void setPayloadType(@NonNull Class<T> payloadType) {
        put(MESSAGE_TYPE_HEADER, List.of(payloadType.getName()));
    }

    /**
     * Retrieves the NATS message ID associated with the headers.
     * The message ID is typically stored under the "Nats-Msg-Id" key in the headers.
     * If the key is not present or its value is null, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the message ID if present, or an empty {@code Optional} otherwise
     */
    @NonNull
    default Optional<String> messageId() {
        return Optional.ofNullable(get(MESSAGE_ID_HEADER)).flatMap(this::getFirst);
    }

    /**
     * Sets the NATS message ID in the headers.
     * This method associates the given message ID with the "Nats-Msg-Id" key
     * in the headers. The message ID can be used to uniquely identify messages
     * in a NATS JetStream context.
     *
     * @param messageId the unique message identifier to be set in the headers
     */
    default void setMessageId(@NonNull String messageId) {
        put(MESSAGE_ID_HEADER, List.of(messageId));
    }

    /**
     * Retrieves the correlation ID associated with the current headers.
     * The correlation ID is typically used to track and correlate messages
     * across different systems or components.
     *
     * @return an {@code Optional} containing the correlation ID if present,
     *         or an empty {@code Optional} if no correlation ID is set
     */
    @NonNull
    default Optional<String> correlationId() {
        return Optional.ofNullable(get(MESSAGE_CORRELATION_ID_HEADER)).flatMap(this::getFirst);
    }

    /**
     * Sets the correlation ID in the headers.
     * The correlation ID is generally used to associate and track messages
     * across different systems or components.
     *
     * @param correlationId the correlation ID to be set in the headers;
     *        must not be null
     */
    default void setCorrelationId(@NonNull String correlationId) {
        put(MESSAGE_CORRELATION_ID_HEADER, List.of(correlationId));
    }

    /**
     * Retrieves the stream name associated with the headers.
     * The stream name typically represents the NATS JetStream stream
     * that the message is associated with.
     * If the stream name is not set or cannot be determined,
     * an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the stream name if present,
     *         or an empty {@code Optional} otherwise
     */
    @NonNull
    default Optional<String> stream() {
        return Optional.ofNullable(get(MESSAGE_STREAM_HEADER)).flatMap(this::getFirst);
    }

    /**
     * Sets the JetStream stream name in the headers.
     * This method associates the specified stream name with the {@code MESSAGE_STREAM_HEADER}
     * key in the headers, storing it as a list with a single value.
     *
     * @param stream the name of the JetStream stream to be set in the headers
     */
    default void setStream(@NonNull String stream) {
        put(MESSAGE_STREAM_HEADER, List.of(stream));
    }

    /**
     * Retrieves the subject associated with the headers.
     * The subject typically represents the NATS subject
     * that the message is associated with.
     * If the subject is not set or cannot be determined,
     * an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the subject if present,
     *         or an empty {@code Optional} otherwise
     */
    @NonNull
    default Optional<String> subject() {
        return Optional.ofNullable(get(MESSAGE_SUBJECT_HEADER)).flatMap(this::getFirst);
    }

    /**
     * Sets the JetStream subject in the headers.
     * This method associates the specified subject with the {@code MESSAGE_SUBJECT_HEADER}
     * key in the headers, storing it as a list with a single value.
     *
     * @param subject the subject to be set in the headers
     */
    default void setSubject(@NonNull String subject) {
        put(MESSAGE_SUBJECT_HEADER, List.of(subject));
    }

    /**
     * Retrieves the reply subject associated with the headers.
     * The reply subject is typically used for handling request-reply patterns
     * in NATS messaging, where a consumer can send a response message back to
     * the originator of the request using the provided reply subject.
     * If the reply subject is not set or cannot be determined, an empty {@code Optional} is returned.
     *
     * @return an {@code Optional} containing the reply subject if present,
     *         or an empty {@code Optional} otherwise
     */
    @NonNull
    default Optional<String> replySubject() {
        return Optional.ofNullable(get(MESSAGE_REPLY_SUBJECT_HEADER)).flatMap(this::getFirst);
    }

    /**
     * Sets the reply subject in the headers.
     * The reply subject is typically used in request-reply messaging patterns,
     * allowing a consumer to respond to the originating sender using the specified subject.
     *
     * @param replySubject the reply subject to be set in the headers; must not be null
     */
    default void setReplySubject(@NonNull String replySubject) {
        put(MESSAGE_REPLY_SUBJECT_HEADER, List.of(replySubject));
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
    default io.nats.client.impl.Headers to() {
        final var result = new io.nats.client.impl.Headers();
        forEach(result::put);
        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<Class<T>> loadClass(String type) {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return Optional.of((Class<T>) classLoader.loadClass(type));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    private Optional<String> getFirst(List<String> values) {
        return values == null || values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
    }
}
