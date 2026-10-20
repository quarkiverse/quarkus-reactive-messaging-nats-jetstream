package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;

/**
 * Represents a consumer for a NATS streaming service. A consumer is responsible
 * for receiving messages from a specified stream. This interface extends the
 * {@code ConsumerConfiguration} interface, inheriting the configuration
 * properties and behaviors for a consumer.
 */
public interface Consumer extends ConsumerConfiguration {

    /**
     * Retrieves the name of the stream associated with this consumer.
     *
     * @return the name of the stream as a String
     */
    String stream();

}
