package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.util.Map;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;

/**
 * Defines the configuration for a datasource. This interface provides access to configurations
 * related to streams, consumers, key-value buckets, and object stores within a datasource.
 */
public interface DataSourceConfiguration {

    /**
     * Retrieves the connection configuration for the datasource.
     * This configuration contains details such as server URIs, authentication
     * credentials, SSL settings, connection timeouts, and other parameters required
     * to establish and manage a connection to the NATS server.
     *
     * @return an {@link Optional} containing the {@link ConnectionConfiguration} for the datasource,
     *         or an empty {@link Optional} if no connection configuration is defined.
     */
    ConnectionConfiguration connection();

    /**
     * The stream configurations. The map key is the name of the stream.
     */
    Map<String, StreamConfiguration> streams();

    /**
     * Retrieves a map containing the consumer configurations for a datasource.
     * The map's keys represent the names of the consumers, and the values are
     * instances of {@link Consumer}, which provide details about each consumer's
     * configuration and associated stream.
     *
     * @return a map where the key is the consumer name and the value is the corresponding {@link Consumer}
     */
    Map<String, Consumer> consumers();

    /**
     * The key value configurations. The map key is the bucket name of the key value.
     */
    Map<String, KeyValueConfiguration> keyValues();

    /**
     * Retrieves configurations for the object stores defined within a datasource.
     * The map's keys represent the names of the object stores, and the values
     * are instances of {@link ObjectStoreConfiguration}, which define the details
     * for each object store's setup and behavior.
     *
     * @return a map where the key is the name of the object store and the value
     *         is the corresponding {@link ObjectStoreConfiguration}.
     */
    Map<String, ObjectStoreConfiguration> objectStores();
}
