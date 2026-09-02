package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;

/**
 * Represents the configuration for the connector that integrates with the messaging system.
 * The configuration is defined under the specified prefix and managed at runtime.
 */
@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ConnectorConfiguration extends DataSourceConfiguration {

    /**
     * Retrieves a map of named datasource configurations. Each entry in the map represents
     * a datasource configuration, where the key is the name of the datasource and the value
     * is the corresponding {@link DataSourceConfiguration} object.
     *
     * @return a map of datasource names to their respective {@link DataSourceConfiguration}.
     */
    @WithName("data-sources")
    Map<String, DataSourceConfiguration> namedDatasource();

}
