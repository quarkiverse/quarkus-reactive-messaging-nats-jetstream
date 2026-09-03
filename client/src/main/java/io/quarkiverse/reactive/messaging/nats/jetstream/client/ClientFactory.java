package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;

/**
 * The {@code ClientFactory} interface provides a contract for creating instances of {@link Client}.
 * Implementations of this factory are responsible for providing a fully initialized and configured
 * {@link Client}, which allows interaction with streams, key-value stores, object stores, and other
 * reactive components in an asynchronous manner.
 */
public interface ClientFactory {

    /**
     * Creates a fully initialized and configured instance of {@link Client}.
     * The method uses the provided connection configuration and executor service
     * to establish and manage the client instance.
     *
     * @param configuration the connection configuration that specifies the server details,
     *        credentials, and other connection parameters; must not be null
     * @param serializer the serializer used to serialize and deserialize messages; must not be null
     * @param executorService the executor service used to manage asynchronous tasks; must not be null
     * @return a non-null instance of {@link Client} that allows interaction with
     *         streams, key-value stores, object stores, and other reactive components
     * @throws ClientException if there is an error during the creation of the client instance
     */
    @NonNull
    Client create(@NonNull ConnectionConfiguration configuration,
            @NonNull Serializer serializer,
            @NonNull ExecutorService executorService) throws ClientException;

}
