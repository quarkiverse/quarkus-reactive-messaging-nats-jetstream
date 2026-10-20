package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Represents a reactive consumer interface for interacting with streams.
 * This interface provides methods for retrieving messages, fetching message batches,
 * retrieving message details, and managing consumer subscriptions.
 */
public interface Consumer {

    /**
     * Retrieves the next available message from the specified stream and consumer within the given timeout period.
     *
     * @param stream the name of the stream from which to retrieve the message; must not be null
     * @param consumer the name of the consumer associated with the stream; must not be null
     * @param timeout the duration within which the next message must be received; must not be null
     * @return a {@link Uni} that resolves to the next {@link Message} if available, or an empty result if no message is
     *         received within the timeout
     */
    @NonNull
    <T> Uni<Message<T>> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout);

    /**
     * Retrieves the next available message from the specified stream and consumer within a given timeout period.
     *
     * @param stream the name of the stream from which to retrieve the message; must not be null
     * @param consumer the name of the consumer associated with the stream; must not be null
     * @param timeout the duration within which the next message must be received; must not be null
     * @param clazz the class type of the message payload; must not be null
     * @return a {@link Uni} that resolves to the next {@link Message} of the specified type if available,
     *         or an empty result if no message is received within the timeout
     */
    @NonNull
    <T> Uni<Message<T>> next(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout,
            @NonNull Class<T> clazz);

    /**
     * Fetches a batch of messages from the specified stream for the given consumer, within the defined timeout period.
     *
     * @param stream the name of the stream from which to fetch messages; must not be null
     * @param consumer the name of the consumer associated with the stream; must not be null
     * @param timeout the maximum duration to wait for fetching messages; must not be null
     * @param batchSize the maximum number of messages to retrieve in a single fetch operation
     * @return a {@link Multi} emitting {@link Message} instances retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> fetch(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);

    /**
     * Fetches a batch of messages from the specified stream for the given consumer within the defined timeout period.
     *
     * @param stream the name of the stream from which to fetch messages; must not be null
     * @param consumer the name of the consumer associated with the stream; must not be null
     * @param timeout the maximum duration to wait for fetching messages; must not be null
     * @param batchSize the maximum number of messages to retrieve in a single fetch operation
     * @param clazz the class type of the message payload; must not be null
     * @return a {@code Multi} emitting {@code Message} instances of the specified type retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> fetch(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize,
            @NonNull Class<T> clazz);

    /**
     * Subscribes to a stream and consumer, continuously receiving messages in batches.
     *
     * @param stream the name of the stream to subscribe to; must not be null
     * @param consumer the name of the consumer associated with the subscription; must not be null
     * @param timeout the maximum duration to wait for receiving messages in each batch; must not be null
     * @param batchSize the maximum number of messages to receive in each batch
     * @return a {@link Multi} emitting {@link Message} instances retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize);

    /**
     * Subscribes to a stream using the specified consumer, continuously receiving messages in batches.
     * This method supports deserialization of message payloads into the specified class type.
     *
     * @param stream the name of the stream to subscribe to; must not be null
     * @param consumer the name of the consumer associated with the subscription; must not be null
     * @param timeout the maximum duration to wait for receiving messages in each batch; must not be null
     * @param batchSize the maximum number of messages to receive in each batch
     * @param clazz the class type of the message payload; must not be null
     * @return a {@code Multi} emitting {@code Message} instances of the specified type retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Duration timeout, int batchSize,
            @NonNull Class<T> clazz);

    /**
     * Subscribes to a stream and consumer, continuously receiving messages pushed by the server.
     *
     * @param stream the name of the stream to subscribe to; must not be null
     * @param consumer the name of the consumer associated with the subscription; must not be null
     * @return a {@link Multi} emitting {@link Message} instances retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer);

    /**
     * Subscribes to a stream using the specified consumer, continuously receiving messages pushed by the server.
     * This method supports deserialization of message payloads into the specified class type.
     *
     * @param stream the name of the stream to subscribe to; must not be null
     * @param consumer the name of the consumer associated with the subscription; must not be null
     * @param clazz the class type of the message payload; must not be null
     * @return a {@code Multi} emitting {@code Message} instances of the specified type retrieved from the stream
     */
    @NonNull
    <T> Multi<Message<T>> subscribe(@NonNull String stream, @NonNull String consumer, @NonNull Class<T> clazz);

}
