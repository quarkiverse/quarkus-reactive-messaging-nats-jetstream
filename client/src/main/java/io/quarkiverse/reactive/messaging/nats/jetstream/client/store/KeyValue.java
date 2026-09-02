package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueEntryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueStatusImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Represents a key-value store interface that supports operations such as retrieval, insertion,
 * update, deletion, purging, and querying status and keys.
 */
public interface KeyValue {

    /**
     * Retrieves the name of the key-value bucket associated with this instance.
     *
     * @return a non-null string representing the name of the key-value bucket
     */
    @NonNull
    String bucketName();

    /**
     * Retrieves the value associated with the specified key from the key-value store.
     *
     * @param key the key to retrieve the value for; must not be null
     * @return a {@link Uni} that resolves to a {@link KeyValueEntryImpl} representing the key-value pair
     */
    @NonNull
    Uni<KeyValueEntry> get(@NonNull String key);

    /**
     * Retrieves the value associated with the specified key at a particular revision from the key-value store.
     *
     * @param key the key to retrieve the value for; must not be null
     * @param revision the specific revision of the key to retrieve
     * @return a {@link Uni} that resolves to a {@link KeyValueEntryImpl} representing the key-value pair at the specified
     *         revision
     */
    @NonNull
    Uni<KeyValueEntry> get(@NonNull String key, long revision);

    /**
     * Stores a key-value pair into the key-value store associated with this instance. If the key
     * already exists, its value will be updated; otherwise, a new key-value pair will be created.
     *
     * @param key the key to store or update; must not be null
     * @param value the value associated with the key; must not be null
     * @return a {@link Uni} that resolves to a {@link KeyValueEntryImpl} representing the stored key-value pair
     */
    @NonNull
    Uni<KeyValueEntry> put(@NonNull String key, byte[] value);

    /**
     * Deletes a key-value pair from the key-value store associated with this instance.
     *
     * @param key the key of the key-value pair to delete; must not be null
     * @return a {@link Uni} that resolves to {@code Void} upon successful deletion
     */
    @NonNull
    Uni<Void> delete(@NonNull String key);

    /**
     * Deletes a key-value pair from the key-value store if the provided revision matches the current revision
     * associated with the specified key.
     *
     * @param key the key of the key-value pair to delete; must not be null
     * @param expectedRevision the revision that must match the current revision of the key for the operation to proceed
     * @return a {@link Uni} that resolves to {@code Void} upon successful deletion or an error if the revision does not match
     */
    @NonNull
    Uni<Void> delete(@NonNull String key, long expectedRevision);

    /**
     * Permanently removes a key and all associated history from the key-value store.
     *
     * @param key the key to purge; must not be null
     * @return a {@link Uni} that resolves to {@code Void} upon successful purge operation
     */
    @NonNull
    Uni<Void> purge(@NonNull String key);

    /**
     * Permanently removes a key and all associated history from the key-value store
     * if the provided revision matches the current revision associated with the specified key.
     *
     * @param key the key to purge; must not be null
     * @param expectedRevision the revision that must match the current revision of the key for the operation to proceed
     * @return a {@link Uni} that resolves to {@code Void} upon successful purge operation
     *         or an error if the revision does not match
     */
    @NonNull
    Uni<Void> purge(@NonNull String key, long expectedRevision);

    /**
     * Retrieves all the keys stored in the key-value bucket associated with this instance.
     *
     * @return a non-null {@link Multi} emitting strings representing the keys in the key-value store
     */
    @NonNull
    Multi<String> keys();

    /**
     * Retrieves the current status of the key-value store, including information about the underlying
     * stream and the key-value configuration.
     *
     * @return a {@link Uni} that resolves to {@link KeyValueStatusImpl}, providing details about the stream
     *         and configuration associated with the key-value store
     */
    @NonNull
    Uni<KeyValueStatus> status();
}
