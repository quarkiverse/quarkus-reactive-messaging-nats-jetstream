package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.jspecify.annotations.NonNull;

/**
 * An object representing a stream's subject and the count of it's messages
 */
public interface Subject {

    /**
     * Get the subject name
     *
     * @return the subject
     */
    @NonNull
    String name();

    /**
     * Get the subject message count
     *
     * @return the count
     */
    long count();
}
