package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration.DEFAULT_DATASOURCE;

import java.util.Collection;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.interceptor.Interceptor;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;
import lombok.extern.jbosslog.JBossLog;

/**
 * Configures the JetStream resources (streams, consumers, key-values, object stores) declared for the default
 * datasource and every named datasource on application startup.
 * <p>
 * Observes {@link StartupEvent} with a priority just before SmallRye Reactive Messaging, which wires the channels in
 * its own {@code StartupEvent} observer at {@link Interceptor.Priority#LIBRARY_BEFORE}, so every resource exists before
 * a channel subscribes to it.
 */
@JBossLog
@ApplicationScoped
public class JetStreamResourceInitializer {
    private final ConnectorConfiguration configuration;
    private final Instance<Client> clients;

    public JetStreamResourceInitializer(ConnectorConfiguration configuration, @Any Instance<Client> clients) {
        this.configuration = configuration;
        this.clients = clients;
    }

    void onStart(@Observes @Priority(Interceptor.Priority.LIBRARY_BEFORE - 1) StartupEvent event) {
        addJetstreamResources(DEFAULT_DATASOURCE, configuration);
        configuration.namedDatasource().forEach(this::addJetstreamResources);
    }

    private void addJetstreamResources(String datasource, DataSourceConfiguration configuration) {
        try {
            final var client = CDIUtils.getInstanceById(clients, datasource).get();
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
}
