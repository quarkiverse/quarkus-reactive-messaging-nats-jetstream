package io.quarkiverse.reactive.messaging.nats;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.AuthHandler;
import io.nats.client.support.SSLUtils;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionOptionsFactory;
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
     * The token to connect to the NATS server
     */
    Optional<String> token();

    /**
     * The path to the credentials file for creating an {@link AuthHandler AuthHandler}
     */
    Optional<String> credentialPath();

    /**
     * Enable SSL connecting to servers
     */
    @WithDefault("true")
    Boolean sslEnabled();

    /**
     * The connection timeout in milliseconds
     */
    Optional<Long> connectionTimeout();

    /**
     * Back-off delay between to attempt to re-connect to NATS
     */
    Optional<Duration> connectionBackoff();

    /**
     * The maximum number of attempts to attempt to re-connect to NATS.
     * The default is -1 which means unlimited.
     * {@value ConnectionOptionsFactory#DEFAULT_MAX_RECONNECT}
     */
    Optional<Integer> connectionAttempts();

    /**
     * The classname for the error listener
     */
    Optional<String> errorListener();

    /**
     * The size in bytes to make buffers for connections
     */
    Optional<Integer> bufferSize();

    /**
     * The path to the keystore file
     */
    Optional<String> keystorePath();

    /**
     * The password for the keystore
     */
    Optional<String> keystorePassword();

    /**
     * The path to the trust store file
     */
    Optional<String> truststorePath();

    /**
     * The password for the trust store
     */
    Optional<String> truststorePassword();

    /**
     * The tls algorithm. Default is {@value SSLUtils#DEFAULT_TLS_ALGORITHM}
     */
    Optional<String> tlsAlgorithm();

    /**
     * The name of the TLS configuration (bucket) used for client authentication in the TLS registry.
     */
    Optional<String> tlsConfigurationName();

}
