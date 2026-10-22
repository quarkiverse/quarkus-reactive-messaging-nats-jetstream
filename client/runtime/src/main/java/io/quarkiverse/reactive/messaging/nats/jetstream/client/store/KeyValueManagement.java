package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.List;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.smallrye.mutiny.Uni;

/**
 * Interface for managing key-value configurations in a system.
 * Provides operations to handle the addition of key-value configurations
 * under specific constraints.
 */
public interface KeyValueManagement {

    /**
     * Adds a key-value configuration to the system if it is not already present.
     * The method does not add the configuration if a configuration with the same
     * key already exists.
     *
     * @param configuration the key-value configuration to be added, must not be null
     * @return a {@code Uni<Void>} that completes when the operation is successfully performed
     */
    @NonNull
    Uni<Void> addIfAbsent(@NonNull KeyValueConfiguration configuration);

    /**
     * Retrieves the list of names of all available key-value buckets in the system.
     * The bucket names represent key-value stores that can be accessed or managed.
     *
     * @return a {@link Uni} emitting a non-null {@link List} of bucket names as {@link String}.
     *         The list may be empty if no buckets are available.
     */
    @NonNull
    Uni<List<String>> bucketNames();
}
