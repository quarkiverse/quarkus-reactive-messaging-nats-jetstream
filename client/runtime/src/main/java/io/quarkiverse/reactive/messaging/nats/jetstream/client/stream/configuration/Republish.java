package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.jspecify.annotations.NonNull;

/**
 * Republish Configuration
 */
public interface Republish {

    /**
     * Get source, the Published subject matching filter
     *
     * @return the source
     */
    @NonNull
    String source();

    /**
     * Get destination, the RePublish Subject template
     *
     * @return the destination
     */
    @NonNull
    String destination();

    /**
     * Get headersOnly, Whether to RePublish only headers (no body)
     *
     * @return headersOnly
     */
    boolean headersOnly();
}
