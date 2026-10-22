package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.time.ZonedDateTime;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.nats.client.api.KeyValueOperation;

/**
 * The KeyValueEntry represents a record in the Key Value history
 */

public interface KeyValueEntry {

    static KeyValueEntry of(io.nats.client.api.KeyValueEntry keyValueEntry) {
        final var mapper = Mappers.getMapper(KeyValueEntryMapper.class);
        return mapper.map(keyValueEntry);
    }

    /**
     * Get the key value bucket this key in.
     *
     * @return the bucket
     */
    @NonNull
    String bucket();

    /**
     * Get the key
     *
     * @return the key
     */
    @NonNull
    String key();

    /**
     * Get the value.
     *
     * @return the value
     */
    @NonNull
    Optional<byte[]> value();

    /**
     * Get the number of bytes in the data. May be zero
     *
     * @return the number of bytes
     */
    long dataLength();

    /**
     * Get the creation time of the current version of the key
     *
     * @return the creation time
     */
    @NonNull
    ZonedDateTime created();

    /**
     * Get the revision number of the string. Not a version, but an internally strictly monotonical value
     *
     * @return the revision
     */
    long revision();

    /**
     * Internal reference to pending message from the entry request
     *
     * @return the delta
     */
    long delta();

    /**
     * The KeyValueOperation of this entry
     *
     * @return the operation
     */
    @NonNull
    KeyValueOperation operation();
}
