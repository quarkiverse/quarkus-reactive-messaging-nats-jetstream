package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;
import io.smallrye.config.WithDefault;

public interface ConnectionConfiguration {

    /**
     * A list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
     */
    @NonNull
    List<String> servers();

    /**
     * The username to connect to the NATS server
     */
    @NonNull
    Optional<String> username();

    /**
     * The password to connect to the NATS server
     */
    @NonNull
    Optional<String> password();

    /**
     * The token to connect to the NATS server
     */
    @NonNull
    Optional<String> token();

    /**
     * The connection timeout
     */
    @NonNull
    Optional<Duration> timeout();

    /**
     * The maximum number of maximumReconnects to attempt to re-connect to NATS.
     * -1 means unlimited.
     */
    @WithDefault("-1")
    int maximumReconnects();

    /**
     * The classname for the error listener
     */
    @NonNull
    Optional<ErrorListener> errorListener();

    /**
     * The size in bytes to make buffers for connections
     */
    @NonNull
    Optional<Integer> bufferSize();

    /**
     * The tls algorithm. Default is {@value "SunX509"}
     */
    @NonNull
    Optional<String> tlsAlgorithm();

    /**
     * The name of the TLS configuration (bucket) used for client authentication in the TLS registry.
     */
    Optional<String> tlsConfigurationName();

    /**
     * The path to the credentials file for creating an AuthHandler
     */
    @NonNull
    Optional<String> credentialPath();
}
