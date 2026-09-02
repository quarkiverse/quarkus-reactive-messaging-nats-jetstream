package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.util.Collection;

import jakarta.enterprise.inject.spi.CDI;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientRegistry;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

/**
 * JetStreamRecorder is responsible for configuring JetStream resources at runtime
 * based on the provided connector configuration. It ensures the necessary resources
 * such as streams, consumers, key-values, and object stores are created or updated.
 * This class interacts with the JetStream management client to provision resources
 * as needed and handles configuration failures by throwing meaningful exceptions.
 */
@JBossLog
@Recorder
@RequiredArgsConstructor
public class JetStreamRecorder {
    private final RuntimeValue<ConnectorConfiguration> configuration;

    /**
     * Sets up the JetStream resources by configuring the primary datasource and named datasources
     * defined in the application configuration.
     * This method retrieves the primary datasource configuration using {@code configuration.getValue().datasource()}
     * and configures the associated JetStream resources such as streams, consumers, key-value stores,
     * and object stores by invoking {@code addJetstreamResources} with the primary datasource configuration.
     * Additionally, it processes all named datasources retrieved from {@code configuration.getValue().namedDatasource()}.
     * For each named datasource configuration, the method invokes {@code addJetstreamResources} to set up
     * the associated JetStream resources.
     * This setup ensures that all declared datasources, both primary and named, are configured
     * and their respective JetStream resources are initialized.
     *
     * @throws RuntimeException if any failures occur during the configuration of JetStream resources.
     */
    public void setup() {
        addJetstreamResources(ClientRegistry.DEFAULT_CLIENT_NAME, configuration.getValue());
        configuration.getValue().namedDatasource().forEach(this::addJetstreamResources);
    }

    private void addJetstreamResources(String datasource, DataSourceConfiguration configuration) {
        try {
            final var client = client(datasource);
            addStreamsIfAbsent(client, configuration.streams().values());
            addConsumersIfAbsent(client, configuration.consumers().values());
            addKeyValuesIfAbsent(client, configuration.keyValues().values());
            addObjectStoresIfAbsent(client, configuration.objectStores().values());
        } catch (Exception failure) {
            throw new RuntimeException(String.format("Failed to configure JetStream resources: %s", failure.getMessage()),
                    failure);
        }
    }

    private void addConsumersIfAbsent(@NonNull Client client,
            @NonNull Collection<Consumer> consumers) {
        consumers.forEach(consumer -> addConsumerIfAbsent(client, consumer.stream(), consumer));
    }

    private void addConsumerIfAbsent(@NonNull Client client, @NonNull String stream,
            @NonNull ConsumerConfiguration configuration) {
        try {
            client.consumerManagement(stream).addIfAbsent(configuration).await().indefinitely();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to configure consumer %s on stream %s: %s", configuration.name(),
                    stream, e.getMessage()), e);
        }
    }

    private void addStreamsIfAbsent(@NonNull Client client, @NonNull Collection<StreamConfiguration> streams) {
        streams.forEach(configuration -> addStreamIfAbsent(client, configuration));
    }

    private void addStreamIfAbsent(@NonNull Client client, @NonNull StreamConfiguration configuration) {
        try {
            client.streamManagement().addIfAbsent(configuration).await().indefinitely();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to configure stream %s: %s", configuration.name(),
                    e.getMessage()), e);
        }
    }

    private void addObjectStoresIfAbsent(@NonNull Client client, @NonNull Collection<ObjectStoreConfiguration> configurations) {
        configurations.forEach(configuration -> addObjectStoreIfAbsent(client, configuration));
    }

    private void addObjectStoreIfAbsent(@NonNull Client client, @NonNull ObjectStoreConfiguration configuration) {
        try {
            client.objectStoreManagement().addIfAbsent(configuration).await().indefinitely();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Failed to configure object store %s: %s", configuration.bucketName(), e.getMessage()), e);
        }
    }

    private void addKeyValuesIfAbsent(@NonNull Client client, @NonNull Collection<KeyValueConfiguration> configurations) {
        configurations.forEach(configuration -> addKeyValueIfAbsent(client, configuration));
    }

    private void addKeyValueIfAbsent(@NonNull Client client, @NonNull KeyValueConfiguration configuration) {
        try {
            client.keyValueManagement().addIfAbsent(configuration).await().indefinitely();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Failed to configure key/value %s: %s", configuration.bucketName(), e.getMessage()), e);
        }
    }

    private @NonNull Client client(@NonNull String datasource) {
        final var clientRegistry = CDI.current().select(ClientRegistry.class).get();
        return clientRegistry.lookup(datasource);
    }
}
