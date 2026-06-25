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
