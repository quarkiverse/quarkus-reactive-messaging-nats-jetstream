package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.List;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.smallrye.mutiny.Uni;

/**
 * Interface for managing object stores, providing functionality
 * to create and manage object store configurations.
 */
public interface ObjectStoreManagement {

    /**
     * Adds an object store with the given configuration if it does not already exist.
     * If the object store already exists, this method does nothing.
     *
     * @param configuration the configuration used to define the object store to be added; must not be null
     * @return a {@link Uni} representing the asynchronous completion of the operation
     */
    @NonNull
    Uni<Void> addIfAbsent(@NonNull ObjectStoreConfiguration configuration);

    /**
     * Retrieves the names of all available object store buckets.
     *
     * @return a {@link Uni} that emits a non-null list of bucket names upon subscription,
     *         or fails with an error if the operation cannot be completed
     */
    @NonNull
    Uni<List<String>> bucketNames();
}
