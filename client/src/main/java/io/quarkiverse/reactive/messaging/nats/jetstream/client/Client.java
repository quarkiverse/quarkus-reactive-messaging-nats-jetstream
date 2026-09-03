package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.Consumer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamManagement;

/**
 * The Client interface provides a unified mechanism to interact with
 * streams, consumers, key-value stores, object stores, and other operations
 * in a reactive and asynchronous manner. It extends the functionalities
 * of {@link Publisher}, {@link Consumer}, and {@link AutoCloseable}.
 * The primary purpose of this API is to provide high-level abstractions
 * for managing and interacting with data streams and related components while
 * ensuring efficient resource handling and reactive programming capabilities.
 */
public interface Client extends Publisher, Consumer, AutoCloseable {

    /**
     * Provides access to the StreamManagement interface for managing
     * streams and associated consumers in a reactive and asynchronous manner.
     *
     * @return a non-null instance of StreamManagement for performing operations
     *         such as creating, deleting, pausing, or resuming consumers,
     *         as well as managing streams, subjects, key-value stores, and object stores.
     */
    @NonNull
    StreamManagement streamManagement();

    /**
     * Provides an interface for managing consumers associated with the specified stream.
     *
     * @param stream the name of the stream for which the consumer management interface is to be provided; must not be null
     * @return a non-null instance of {@link ConsumerManagement} for the specified stream, allowing operations related to
     *         consumer management
     */
    @NonNull
    ConsumerManagement consumerManagement(@NonNull String stream);

    /**
     * Provides access to the {@link KeyValueManagement} interface for managing
     * key-value configurations in the system.
     *
     * @return a non-null instance of {@link KeyValueManagement}, which allows
     *         performing operations such as adding key-value configurations under specific constraints.
     */
    @NonNull
    KeyValueManagement keyValueManagement();

    /**
     * Provides access to the {@link ObjectStoreManagement} interface for managing
     * object stores and their configurations in a reactive and asynchronous manner.
     *
     * @return a non-null instance of {@link ObjectStoreManagement}, allowing operations
     *         such as creating, managing, and configuring object stores in the system
     */
    @NonNull
    ObjectStoreManagement objectStoreManagement();

    /**
     * Provides access to an object store (bucket) identified by the given bucket name. The returned
     * {@link ObjectStore} interface allows managing objects within the specified bucket, including
     * operations such as adding, retrieving, updating, deleting, or sealing objects.
     *
     * @param bucketName the name of the object store (bucket) to access; must not be null
     * @return a non-null instance of {@link ObjectStore} for the specified bucket name
     */
    @NonNull
    ObjectStore objectStore(@NonNull String bucketName);

    /**
     * Provides access to a key-value store (bucket) identified by the given bucket name. The returned
     * {@link KeyValue} interface allows managing key-value entries within the specified bucket, including
     * operations such as retrieving, storing, deleting, and listing keys.
     *
     * @param bucketName the name of the key-value store (bucket) to access; must not be null
     * @return a non-null instance of {@link KeyValue} for the specified bucket name
     */
    @NonNull
    KeyValue keyValue(@NonNull String bucketName);

    /**
     * Indicates whether the current client instance has been closed.
     * Once closed, the client should no longer be used for operations,
     * and any subsequent method calls may result in errors or undefined behavior.
     *
     * @return {@code true} if the client has been closed, otherwise {@code false}.
     */
    boolean closed();

}
