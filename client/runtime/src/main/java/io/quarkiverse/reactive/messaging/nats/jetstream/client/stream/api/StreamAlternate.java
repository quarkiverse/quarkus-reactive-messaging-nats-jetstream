package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface StreamAlternate {

    /**
     * The mirror stream name
     *
     * @return the name
     */
    @NonNull
    String name();

    /**
     * The domain
     *
     * @return the domain
     */
    @NonNull
    Optional<String> domain();

    /**
     * The name of the cluster holding the stream
     *
     * @return the cluster
     */
    @NonNull
    String cluster();
}
