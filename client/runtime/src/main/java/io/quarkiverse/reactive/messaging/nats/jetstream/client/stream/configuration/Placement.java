package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Placement directives to consider when placing replicas of a stream
 */
public interface Placement {

    /**
     * The desired cluster name to place the stream.
     *
     * @return The cluster name
     */
    @NonNull
    Optional<String> cluster();

    /**
     * Tags required on servers hosting this stream
     *
     * @return the list of tags
     */
    @NonNull
    List<String> tags();

}
