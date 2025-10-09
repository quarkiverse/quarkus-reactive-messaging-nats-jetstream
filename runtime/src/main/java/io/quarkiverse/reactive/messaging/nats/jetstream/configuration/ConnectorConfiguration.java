package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.util.Map;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ConnectorConfiguration {

    /**
     * The connection configuration
     */
    ConnectionConfiguration connection();

    /**
     * Enable tracing for JetStream
     */
    @WithDefault("true")
    Boolean trace();

    /**
     * Whether the streams and key value stores should be automatically configured.
     * If the artifacts already exist, they are left untouched.
     */
    @WithDefault("true")
    Boolean autoConfiguration();

    /**
     * The stream configurations. The map key is the name of the stream.
     */
    Map<String, StreamConfiguration> streams();

    /**
     * The configuration of key value stores. The key is the name of the bucket.
     */
    Map<String, KeyValueStoreConfiguration> keyValueStores();

}
