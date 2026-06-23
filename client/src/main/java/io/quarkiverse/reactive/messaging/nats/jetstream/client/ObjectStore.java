package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface ObjectStore {

    @NonNull String bucketName();


    /**
     * Place the contents of the input stream into a new object.
     * @param metadata the metadata for the object
     * @param data the serialized object data
     * @return the ObjectInfo for the saved object
     */
    @NonNull Uni<ObjectInfo> put(@NonNull ObjectMetadata metadata, byte @NonNull[] data);

    /**
     * Place the contents of the input stream into a new object.
     * @param objectName the name of the object
     * @param data the serialized object data
     * @return the ObjectInfo for the saved object
     */
    @NonNull Uni<ObjectInfo> put(@NonNull String objectName, byte @NonNull [] data);

    /**
     * Get an object by name from the store, reading it into the output stream, if the object exists.
     * @param objectName The name of the object
     * @return the ObjectEntry for the object name.
     */
    @NonNull Uni<ObjectEntry> get(@NonNull String objectName);

    /**
     * Get the info for an object if the object exists / is not deleted.
     * @param objectName The name of the object
     * @return the ObjectInfo for the object name or throw an exception if it does not exist.
     */
    @NonNull Uni<ObjectInfo> info(@NonNull String objectName);

    /**
     * Get the info for an object if the object exists, optionally including deleted.
     * @param objectName The name of the object
     * @param includingDeleted whether to return info for deleted objects
     * @return the ObjectInfo for the object name or throw an exception if it does not exist.
     */
    @NonNull Uni<ObjectInfo> info(@NonNull String objectName, boolean includingDeleted);

    /**
     * Update the metadata of name, description or headers. All other changes are ignored.
     * @param objectName The name of the object
     * @param metadata the metadata with the new or unchanged name, description and headers.
     * @return the ObjectInfo after update
     */
    @NonNull Uni<ObjectInfo> update(@NonNull String objectName, @NonNull ObjectMetadata metadata);

    /**
     * Delete the object by name. A No-op if the object is already deleted.
     * @param objectName The name of the object
     * @return the ObjectInfo after delete.
     */
    @NonNull Uni<ObjectInfo> delete(@NonNull String objectName);

    /**
     * Add a link to another object. A link cannot be for another link.
     * @param objectName The name of the object
     * @param toInfo the info object of the object to link to
     * @return the ObjectInfo for the link as saved
     */
    @NonNull Uni<ObjectInfo> link(@NonNull String objectName, @NonNull ObjectInfo toInfo);

    /**
     * Add a link to another object store (bucket).
     * @param objectName The name of the object
     * @param store the store object to link to
     * @return the ObjectInfo for the link as saved or throws an exception
     */
    @NonNull Uni<ObjectInfo> link(@NonNull String objectName, @NonNull ObjectStore store);

    /**
     * Close (seal) the bucket to changes. The store (bucket) will be read only.
     * @return the status object
     */
    @NonNull Uni<ObjectStoreStatus> seal();

    /**
     * Get the ObjectStoreStatus object.
     * @return the status object
     */
    @NonNull Uni<ObjectStoreStatus> status();

    /**
     * Get a list of all object [infos] in the store.
     * @return the list of objects
     */
    @NonNull Multi<ObjectInfo> list();

}
