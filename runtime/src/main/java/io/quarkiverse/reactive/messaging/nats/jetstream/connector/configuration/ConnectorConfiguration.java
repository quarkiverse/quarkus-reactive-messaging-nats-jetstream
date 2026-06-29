package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

import java.util.Map;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ConnectorConfiguration {

    /**
     * Configuration for the datasource.
     */
    DatasourceConfiguration datasource();

    /**
     * The stream configurations. The map key is the name of the stream.
     */
    Map<String, StreamConfiguration> streams();

    /**
     * The consumer configurations. The map key is the name of the consumer.
     */
    Map<String, ConsumerConfiguration> consumers();

    /**
     * The key value configurations. The map key is the bucket name of the key value.
     */
    Map<String, KeyValueConfiguration> keyValues();

    interface DatasourceConfiguration extends ConnectionConfiguration {

        /**
         * Retrieves a map of named connection configurations. The keys in the map represent
         * the names of the connections, and the values are the corresponding connection
         * configuration details.
         *
         * @return a map where the keys are connection names and the values are instances
         *         of {@code ConnectionConfiguration} containing configuration details for
         *         each connection.
         */
        @WithName("")
        Map<String, ConnectionConfiguration> named();
    }
}
