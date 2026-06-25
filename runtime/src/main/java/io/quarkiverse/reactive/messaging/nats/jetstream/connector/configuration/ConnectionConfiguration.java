package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;

import javax.net.ssl.SSLContext;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for a single connection to a NATS cluster.
 */
public interface ConnectionConfiguration {

    /**
     * A list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
     */
    List<String> servers();

    /**
     * The username to connect to the NATS server
     */
    Optional<String> username();

    /**
     * The password to connect to the NATS server
     */
    Optional<String> password();

    /**
     * The token to connect to the NATS server
     */
    Optional<String> token();

    /**
     * The connection timeout
     */
    Optional<Duration> timeout();

    /**
     * The maximum number of maximumReconnects to attempt to re-connect to NATS.
     * If Optional.empty() means unlimited.
     */
    Optional<Integer> maximumReconnects();

    /*
     * The class for the error listener
     */
    Optional<ErrorListener> errorListener();

    /**
     * The size in bytes to make buffers for connections
     */
    Optional<Integer> bufferSize();

    /**
     * The tls algorithm. Default is {@value "SunX509"}
     */
    Optional<String> tlsAlgorithm();

    /**
     * The name of the TLS configuration used for client authentication in the TLS registry.
     */
    Optional<String> tlsConfigurationName();

    /**
     * The path to the credentials file for creating an AuthHandler
     */
    Optional<String> credentialPath();
}
