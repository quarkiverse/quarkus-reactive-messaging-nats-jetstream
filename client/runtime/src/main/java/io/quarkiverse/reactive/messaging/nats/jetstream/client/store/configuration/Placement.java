package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

/**
 * Represents a placement configuration for streams in a NATS JetStream context.
 * The placement configuration specifies directives such as the cluster to use
 * and any server tags required for the placement of replicas or data streams.
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
