package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.jspecify.annotations.NonNull;

/**
 * SubjectTransform
 */
public interface SubjectTransform {

    /**
     * Get source, the subject matching filter
     *
     * @return the source
     */
    @NonNull
    String source();

    /**
     * Get destination, the SubjectTransform Subject template
     *
     * @return the destination
     */
    @NonNull
    String destination();
}
