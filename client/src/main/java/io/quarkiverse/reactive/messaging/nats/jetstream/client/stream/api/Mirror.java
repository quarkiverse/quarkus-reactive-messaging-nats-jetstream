package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.SubjectTransform;

/**
 * Information about an upstream stream source in a mirror
 */
public interface Mirror {

    /**
     * The name of the Stream being replicated
     *
     * @return the name
     */
    @NonNull
    String name();

    /**
     * The subject filter to apply to the messages
     *
     * @return the subject filter
     */
    @NonNull
    Optional<String> filterSubject();

    /**
     * How many uncommitted operations this peer is behind the leader
     *
     * @return the lag
     */
    long lag();

    /**
     * Time since this peer was last seen, or null if there is no information
     *
     * @return the time
     */
    @NonNull
    Optional<Duration> active();

    /**
     * Configuration referencing a stream source in another account or JetStream domain
     *
     * @return the external
     */
    @Nullable
    Optional<External> external();

    /**
     * The list of subject transforms, if any
     *
     * @return the list of subject transforms
     */
    @NonNull
    List<SubjectTransform> subjectTransforms();

    /**
     * The last error
     *
     * @return the error
     */
    @NonNull
    Optional<Error> error();
}
