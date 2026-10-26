package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface ObjectLink {

    /**
     * Get the bucket the linked object is in
     *
     * @return the bucket name
     */
    @NonNull
    String bucket();

    /**
     * Get the name of the object for the link
     *
     * @return the object name
     */
    @NonNull
    Optional<String> objectName();

    /**
     * True if the object is a link to an object versus a link to a bucket
     *
     * @return true if the object is a link
     */
    boolean isObjectLink();

    /**
     * True if the object is a bucket to an object versus a link to a link
     *
     * @return true if the object is a bucket
     */
    boolean isBucketLink();

}
