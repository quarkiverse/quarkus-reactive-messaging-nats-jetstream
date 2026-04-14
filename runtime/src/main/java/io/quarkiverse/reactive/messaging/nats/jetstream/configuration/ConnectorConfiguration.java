package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.util.Map;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ConnectorConfiguration {

    /**
     * The connection configuration
     */
    ConnectionConfiguration connection();

    /**
     * The stream configurations. The map key is the name of the stream.
     */
    Map<String, Stream> streams();

    /**
     * The configuration of key value stores. The key is the name of the bucket.
     */
    Map<String, KeyValueStoreConfiguration> keyValueStores();

}
