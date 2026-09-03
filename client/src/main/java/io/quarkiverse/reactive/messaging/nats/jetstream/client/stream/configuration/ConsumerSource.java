package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.jspecify.annotations.NonNull;

/**
 * Consumer information for durable sourcing. Dictates that a durable consumer with a specific
 * name is used for sourcing.
 */
public interface ConsumerSource {

    /**
     * The durable consumer name used for sourcing.
     *
     * @return the consumer name
     */
    @NonNull
    String name();

    /**
     * The subject to deliver messages to.
     *
     * @return the deliver subject
     */
    @NonNull
    String deliverSubject();
}
