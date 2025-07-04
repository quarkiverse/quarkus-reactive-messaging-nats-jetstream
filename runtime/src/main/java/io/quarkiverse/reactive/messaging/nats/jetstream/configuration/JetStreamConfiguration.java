package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.util.Map;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface JetStreamConfiguration {

    /**
     * The connection configuration
     */
    ConnectionConfiguration connection();

    /**
     * Automatically configure streams, consumers and key value stores.
     * If a stream or a consumer already exists and the configuration does not match the existing configuration, then an
     * exception is thrown.
     */
    @WithDefault("true")
    Boolean autoConfigure();

    /**
     * Enable tracing for JetStream
     */
    @WithDefault("true")
    Boolean trace();

    /**
     * The stream configurations. The map key is the name of the stream.
     */
    Map<String, StreamConfiguration> streams();

    /**
     * The configuration of key value stores. The key is the name of the bucket.
     */
    Map<String, KeyValueStoreConfiguration> keyValueStores();

}
