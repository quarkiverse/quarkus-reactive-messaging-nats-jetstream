package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import javax.net.ssl.SSLContext;

/**
 * Represents a context for configuring and utilizing Transport Layer Security (TLS)
 * within the application. This interface provides access to the {@link SSLContext},
 * which serves as the standard implementation for secure communication using TLS protocols.
 * Implementations of this interface are expected to encapsulate and manage the configuration
 * required for establishing secure connections. The {@link SSLContext} returned by the
 * {@link #sslContext()} method may be used in constructing secure communication channels.
 */
public interface TlsContext {

    /**
     * Provides access to the {@link SSLContext} for secure communication using TLS protocols.
     * The returned {@link SSLContext} can be used to configure secure connections within the application.
     *
     * @return an instance of {@link SSLContext} representing the configuration for secure TLS communication
     */
    SSLContext sslContext();

    /**
     * Whether the client verifies that the server certificate is issued for the host it connects to.
     * Disabled when the TLS configuration sets {@code hostname-verification-algorithm} to {@code NONE}.
     *
     * @return {@code true} if the server hostname is verified
     */
    boolean verifyHostname();
}
