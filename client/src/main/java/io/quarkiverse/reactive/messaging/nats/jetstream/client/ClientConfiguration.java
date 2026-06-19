package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

import lombok.Builder;
import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageConfiguration;

/**
 * The {@code ClientConfiguration} defines the configuration settings for a client
 * that interacts with a NATS messaging system. This configuration provides access to configuration
 * details related to connections, message handling, and the execution environment.
 */
@Builder
public record ClientConfiguration(
        /*
         * Retrieves the {@link ConnectionConfiguration} associated with the client.
         * The {@link ConnectionConfiguration} contains essential details for establishing
         * and managing connections to the NATS cluster, such as servers, credentials,
         * connection timeout, retry mechanisms, TLS settings, and more.
         */
        @NonNull
        ConnectionConfiguration connectionConfiguration,
        /*
         * Retrieves an optional {@link MessageConfiguration} that contains configuration settings
         * related to message handling for the client.
         * If the {@link MessageConfiguration} is available, it may include details such as
         * acknowledgment timeouts and other message-specific behaviors.
         */
        @NonNull
        MessageConfiguration messageConfiguration,
        /*
         * Provides an {@link ExecutorService} instance used for asynchronous task execution.
         */
        @NonNull
        ExecutorService executorService) {

    public ClientConfiguration {
        Objects.requireNonNull(connectionConfiguration, "connectionConfiguration");
        Objects.requireNonNull(messageConfiguration, "messageConfiguration");
        Objects.requireNonNull(executorService, "executorService");
    }

}
