package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

public interface TlsContextFactory {

    /**
     * Creates a {@link TlsContext} instance based on the provided name.
     * This method is intended to set up the context for secure communication
     * using Transport Layer Security (TLS) as required by the application.
     *
     * @param name The name of the TLS configuration
     * @return an instance of {@link TlsContext} representing the configured
     *         TLS context for secure communication
     */
    TlsContext create(String name);
}
