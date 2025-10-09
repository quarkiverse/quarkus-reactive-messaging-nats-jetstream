package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.nats.client.AuthHandler;
import io.nats.client.ErrorListener;
import io.nats.client.support.SSLUtils;
import io.smallrye.config.WithDefault;

public interface ConnectionConfiguration {

    /**
     * A comma separated list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
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
     * Enable SSL connecting to servers
     */
    Optional<Boolean> sslEnabled();

    /**
     * The connection timeout
     */
    Optional<Duration> connectionTimeout();

    /**
     * Back-off delay between to attempt to re-connect to NATS
     */
    Optional<Duration> connectionBackoff();

    /**
     * The maximum number of attempts to attempt to re-connect to NATS.
     * The default is -1 that means unlimited.
     */
    @WithDefault("-1")
    Integer connectionAttempts();

    /**
     * The classname for the error listener
     */
    Optional<ErrorListener> errorListener();

    /**
     * The size in bytes to make buffers for connections
     */
    Optional<Integer> bufferSize();

    /**
     * The tls algorithm. Default is {@value SSLUtils#DEFAULT_TLS_ALGORITHM}
     */
    Optional<String> tlsAlgorithm();

    /**
     * The name of the TLS configuration (bucket) used for client authentication in the TLS registry.
     */
    Optional<String> tlsConfigurationName();

    /**
     * The path to the credentials file for creating an {@link AuthHandler AuthHandler}
     */
    Optional<String> credentialPath();

}
