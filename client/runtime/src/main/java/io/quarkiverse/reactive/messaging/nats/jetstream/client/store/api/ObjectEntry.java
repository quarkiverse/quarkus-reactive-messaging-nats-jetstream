package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import org.jspecify.annotations.NonNull;

/**
 * Represents an object entry within a storage or metadata system.
 * This interface provides methods to retrieve the data and metadata
 * associated with the object entry.
 */
public interface ObjectEntry {

    static ObjectEntry of(byte @NonNull [] data, @NonNull ObjectInfo info) {
        return new ObjectEntryImpl(data, info);
    }

    /**
     * Retrieves the data associated with this object entry as a non-null byte array.
     *
     * @return a non-null byte array representing the data of the object entry
     */
    byte @NonNull [] data();

    /**
     * Retrieves the metadata and instance information associated with this object entry.
     *
     * @return a non-null {@link ObjectInfo} instance containing the metadata and instance details
     */
    @NonNull
    ObjectInfo info();

}
