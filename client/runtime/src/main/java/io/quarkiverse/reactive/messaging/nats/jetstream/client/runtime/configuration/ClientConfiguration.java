package io.quarkiverse.reactive.messaging.nats.jetstream.client.runtime.configuration;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Runtime configuration of the NATS client: the default datasource (at the root) and every named datasource.
 * Kept under the {@code quarkus.messaging.nats} prefix for compatibility with the messaging connector.
 */
@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ClientConfiguration extends DataSourceConfiguration {

    /** The datasource name used when a channel or resource does not specify one explicitly. */
    String DEFAULT_DATASOURCE = "default";

    /**
     * Retrieves a map of named datasource configurations. Each entry in the map represents
     * a datasource configuration, where the key is the name of the datasource and the value
     * is the corresponding {@link DataSourceConfiguration} object.
     *
     * @return a map of datasource names to their respective {@link DataSourceConfiguration}.
     */
    @WithName("data-sources")
    Map<String, DataSourceConfiguration> namedDatasource();

    /**
     * Whether the message payload is recorded as the {@code messaging.nats.message.payload} attribute of tracing
     * spans. Disabled by default, since payloads may contain sensitive data and can be arbitrarily large.
     */
    @WithDefault("false")
    boolean tracePayload();

}
