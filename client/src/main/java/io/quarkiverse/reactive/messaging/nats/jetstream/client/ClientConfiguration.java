package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageConfiguration;

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
    ConnectionConfiguration connectionConfiguration();

    /**
     * Retrieves an optional {@link MessageConfiguration} that contains configuration settings
     * related to message handling for the client.
     * If the {@link MessageConfiguration} is available, it may include details such as
     * acknowledgment timeouts and other message-specific behaviors.
     *
     * @return a non-null {@code Optional} containing the {@link MessageConfiguration} if specified,
     *         or an empty {@code Optional} if no message-specific configuration is defined.
     */
    @NonNull
    MessageConfiguration messageConfiguration();

    /**
     * Provides an {@link ExecutorService} instance used for asynchronous task execution.
     *
     * @return a non-null {@link ExecutorService} which is configured for handling tasks within the client.
     */
    @NonNull
    ExecutorService executorService();
}
