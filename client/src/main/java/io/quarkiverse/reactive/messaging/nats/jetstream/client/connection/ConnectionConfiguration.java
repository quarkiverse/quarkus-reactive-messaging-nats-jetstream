package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.jspecify.annotations.NonNull;

import io.smallrye.config.WithDefault;

public interface ConnectionConfiguration {
    /**
     * A list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
     *
     * @return the list of servers
     */
    @NonNull
    List<String> servers();

    /**
     * The username to connect to the NATS server
     *
     * @return the username
     */
    @NonNull
    Optional<String> username();

    /**
     * The password to connect to the NATS server
     *
     * @return the password
     */
    @NonNull
    Optional<String> password();

    /**
     * The token to connect to the NATS server
     *
     * @return the token
     */
    @NonNull
    Optional<String> token();

    /**
     * The connection timeout
     *
     * @return the connection timeout
     */
    @NonNull
    Optional<Duration> timeout();

    /**
     * The maximum number of maximumReconnects to attempt to re-connect to NATS.
     * If -1 means unlimited.
     *
     * @return the maximum number of maximumReconnects
     */
    @WithDefault("-1")
    int maximumReconnects();

    /**
     * The implementation of the ErrorListener interface
     *
     * @return The implementation of the ErrorListener interface
     */
    @NonNull
    Optional<ErrorListener> errorListener();

    /**
     * The size in bytes to make buffers for connections
     *
     * @return the size in bytes to make buffers for connections
     */
    @NonNull
    Optional<Integer> bufferSize();

    /**
     * The tls algorithm. Default is {@value "SunX509"} when not set
     *
     * @return the tls algorithm
     */
    @NonNull
    Optional<String> tlsAlgorithm();

    /**
     * The ssl context.
     *
     * @return the ssl context
     */
    @NonNull
    Optional<SSLContext> sslContext();

    /**
     * The path to the credentials file for creating an AuthHandler
     *
     * @return the path to the credentials file for creating an AuthHandler
     */
    @NonNull
    Optional<String> credentialPath();

}
