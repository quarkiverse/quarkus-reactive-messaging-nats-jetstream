package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.MessageConfiguration;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.ExecutorService;

/**
 * The {@code ClientConfiguration} interface defines the configuration settings for a client
 * that interacts with a NATS messaging system. This interface provides access to configuration
 * details related to connections, message handling, and the execution environment.
 */
public interface ClientConfiguration {

    /**
     * Retrieves the {@link ConnectionConfiguration} associated with the client.
     * The {@link ConnectionConfiguration} contains essential details for establishing
     * and managing connections to the NATS cluster, such as servers, credentials,
     * connection timeout, retry mechanisms, TLS settings, and more.
     *
     * @return a non-null {@link ConnectionConfiguration} instance used for configuring and managing the connection.
     */
    @NonNull
    ConnectionConfiguration connection();

    /**
     * Retrieves the {@link MessageConfiguration} associated with the client.
     * The {@link MessageConfiguration} provides details about message handling,
     * such as acknowledgment timeouts, backoff strategies, payload mapping, and tracing configurations.
     *
     * @return a non-null {@link MessageConfiguration} instance used for configuring and managing messages.
     */
    @NonNull
    MessageConfiguration message();

    /**
     * Provides an {@link ExecutorService} instance used for asynchronous task execution.
     *
     * @return a non-null {@link ExecutorService} which is configured for handling tasks within the client.
     */
    @NonNull
    ExecutorService executorService();
}
