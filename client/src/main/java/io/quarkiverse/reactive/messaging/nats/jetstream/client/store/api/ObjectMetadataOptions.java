package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * The ObjectMetaOptions are additional options describing the object
 */
public interface ObjectMetadataOptions {

    /**
     * Get the link this object refers to
     *
     * @return the link or empty() if this is not a link object
     */
    @NonNull
    Optional<ObjectLink> link();

    /**
     * Get the chunk size
     *
     * @return the chunk size in bytes
     */
    @NonNull
    Integer chunkSize();
}
