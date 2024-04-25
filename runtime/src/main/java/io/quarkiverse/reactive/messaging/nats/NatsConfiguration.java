package io.quarkiverse.reactive.messaging.nats;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface NatsConfiguration {

    /**
     * A comma-separated list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
     */
    String servers();

    /**
     * The username to connect to the NATS server
     */
    Optional<String> username();

    /**
     * The password to connect to the NATS server
     */
    Optional<String> password();

    /**
     * Enable SSL connecting to servers
     */
    @WithDefault("true")
    Boolean sslEnabled();

    /**
     * The maximum number of reconnect attempts
     */
    Optional<Integer> maxReconnects();

    /**
     * The connection timeout in milliseconds
     */
    Optional<Long> connectionTimeout();

    /**
     * The classname for the error listener
     */
    Optional<String> errorListener();

    /**
     * The size in bytes to make buffers for connections
     */
    Optional<Integer> bufferSize();

}
