package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.PurgeResult;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api.Stream;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Interface for managing streams and their associated consumers in a reactive and asynchronous manner.
 * Provides operations for creating, deleting, pausing, and resuming consumers, as well as managing streams,
 * subjects, key-value stores, and object stores.
 */
public interface StreamManagement {

    /**
     * Retrieves detailed information about a specific message in a stream, given its sequence number.
     *
     * @param stream the name of the stream from which the message should be retrieved; must not be null
     * @param sequence the sequence number of the message to fetch from the stream
     * @return a {@link Uni} that resolves to {@link Message} containing details about the specified message
     */
    @NonNull
    Uni<Message> message(@NonNull String stream, long sequence);

    /**
     * Purges the specified stream, removing all messages from it based on the server's purge policies.
     * The operation is performed asynchronously and returns the result of the purge.
     *
     * @param stream the name of the stream to be purged; must not be null
     * @return a {@link Uni} that resolves to {@link PurgeResult} containing details about the purge operation,
     *         including the stream name, success status, and the number of messages purged
     */
    @NonNull
    Uni<PurgeResult> purge(@NonNull String stream);

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     */
    @NonNull
    Uni<Void> deleteMessage(@NonNull String stream, long sequence, boolean erase);

    /**
     * Purges all available streams, removing their data based on the server's purge policies.
     * Returns a reactive stream emitting the results of the purge operation for each stream.
     *
     * @return a {@link Multi} emitting {@link PurgeResult} objects, where each result represents
     *         the outcome of the purge operation for an individual stream, including the stream name,
     *         the success status, and the number of items purged.
     */
    @NonNull
    Multi<PurgeResult> purgeAll();

    /**
     * Adds a subject to the specified stream. This operation allows a new subject to be associated
     * with an existing stream, enabling the stream to receive messages matching the subject.
     *
     * @param stream the name of the stream to which the subject will be added; must not be null
     * @param subject the subject to be added to the stream; must not be null
     * @return a {@link Uni} that resolves to {@link Stream} containing the updated details
     *         of the stream after the subject has been added
     */
    @NonNull
    Uni<Stream> addSubject(@NonNull String stream, @NonNull String subject);

    /**
     * Removes a subject from the specified stream. This operation dissociates the
     * given subject from the stream, preventing the stream from receiving messages
     * matching the subject.
     *
     * @param stream the name of the stream from which the subject will be removed; must not be null
     * @param subject the subject to be removed from the stream; must not be null
     * @return a {@link Uni} that resolves to {@link Stream} containing the updated
     *         details of the stream after the subject has been removed
     */
    @NonNull
    Uni<Stream> removeSubject(@NonNull String stream, @NonNull String subject);

    /**
     * Adds a new stream specified by the given configuration if it does not already exist.
     * This operation ensures that no duplicate streams are created based on the provided configuration.
     *
     * @param configuration the configuration for the stream to be added; must not be null
     * @return a {@link Uni} that resolves to {@link Stream} containing details of the stream,
     *         either newly created or already existing
     */
    @NonNull
    Uni<Stream> addIfAbsent(@NonNull StreamConfiguration configuration);

    /**
     * Retrieves information about the specified stream in a reactive and asynchronous manner.
     *
     * @param stream the name of the stream to retrieve information for; must not be null
     * @return a {@link Uni} instance that resolves to {@link Stream} containing details about the specified stream
     */
    @NonNull
    Uni<Stream> stream(@NonNull String stream);

    /**
     * Retrieves information about all available streams in a reactive and asynchronous manner.
     *
     * @return a {@link Multi} instance emitting {@link Stream} objects representing
     *         the metadata and configuration details of each stream.
     */
    @NonNull
    Multi<Stream> streams();

}
