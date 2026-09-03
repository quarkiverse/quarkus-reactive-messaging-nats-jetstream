package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import org.jspecify.annotations.NonNull;

public interface Error {

    /**
     * The request error code from the server
     *
     * @return the code
     */
    int code();

    /**
     * The api error code from the server
     *
     * @return the code
     */
    int apiErrorCode();

    /**
     * Get the error description
     *
     * @return the description
     */
    @NonNull
    String description();
}
