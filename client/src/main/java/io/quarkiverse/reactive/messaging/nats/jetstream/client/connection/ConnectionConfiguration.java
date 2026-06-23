package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.net.ssl.SSLContext;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ConnectionConfiguration(
        /*
         * A list of URI's nats://{host}:{port} to use for establishing the initial connection to the NATS cluster.
         */
        @NonNull List<String> servers,
        /*
         * The username to connect to the NATS server
         */
        @NonNull Optional<String> username,
        /*
         * The password to connect to the NATS server
         */
        @NonNull Optional<String> password,
        /*
         * The token to connect to the NATS server
         */
        @NonNull Optional<String> token,
        /*
         * The connection timeout
         */
        @NonNull Optional<Duration> timeout,
        /*
         * The maximum number of maximumReconnects to attempt to re-connect to NATS.
         * If Optional.empty() means unlimited.
         */
        @NonNull Optional<Integer> maximumReconnects,
        /*
         * The classname for the error listener
         */
        @NonNull Optional<ErrorListener> errorListener,
        /*
         * The size in bytes to make buffers for connections
         */
        @NonNull Optional<Integer> bufferSize,
        /*
         * The tls algorithm. Default is {@value "SunX509"}
         */
        @NonNull Optional<String> tlsAlgorithm,
        /*
         * The ssl context.
         */
        @NonNull Optional<SSLContext> sslContext,
        /*
         * The path to the credentials file for creating an AuthHandler
         */
        @NonNull Optional<String> credentialPath) {

    public ConnectionConfiguration {
        Objects.requireNonNull(servers, "servers");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(password, "password");
        Objects.requireNonNull(token, "token");
        Objects.requireNonNull(timeout, "timeout");
        Objects.requireNonNull(maximumReconnects, "maximumReconnects");
        Objects.requireNonNull(errorListener, "errorListener");
        Objects.requireNonNull(bufferSize, "bufferSize");
        Objects.requireNonNull(tlsAlgorithm, "tlsAlgorithm");
        Objects.requireNonNull(sslContext, "sslContext");
        Objects.requireNonNull(credentialPath, "credentialPath");
    }

}
