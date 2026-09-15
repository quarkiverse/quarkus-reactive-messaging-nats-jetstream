package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration.DEFAULT_DATASOURCE;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.VertxClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ConnectionConfigurationMapper;
import io.quarkus.arc.SyntheticCreationalContext;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

/**
 * JetStreamRecorder has two responsibilities:
 * <ul>
 * <li>it supplies the bean-creation function used by the deployment processor to register one
 * {@code @ApplicationScoped}, {@code @Identifier}-qualified {@link Client} CDI bean per configured datasource
 * ({@link #createClient(String)}); and</li>
 * <li>once those beans exist, it configures the JetStream resources (streams, consumers, key-values, object
 * stores) declared for each datasource ({@link #setup()}).</li>
 * </ul>
 */
@JBossLog
@Recorder
@RequiredArgsConstructor
public class JetStreamRecorder {
    private final RuntimeValue<ConnectorConfiguration> configuration;

    /**
     * Returns the synthetic-bean creation function for the {@link Client} of the given datasource. Invoked by
     * {@code JetStreamProcessor} at build time, once per configured datasource, to back a
     * {@code SyntheticBeanBuildItem}; the returned function itself only runs at runtime, when CDI actually
     * instantiates the bean.
     *
     * @param datasource the datasource name; either {@link ConnectorConfiguration#DEFAULT_DATASOURCE} or one of the
     *        keys of {@code quarkus.messaging.nats.data-sources}
     */
    public Function<SyntheticCreationalContext<Client>, Client> createClient(String datasource) {
        return context -> {
            final var dataSourceConfiguration = dataSourceConfiguration(datasource);
            final var connectionConfigurationMapper = CDI.current().select(ConnectionConfigurationMapper.class).get();
            final var serializer = CDI.current().select(Serializer.class).get();
            final var tracerFactory = CDI.current().select(TracerFactory.class).get();
            final var vertx = CDI.current().select(Vertx.class).get();
            final var executorService = CDI.current().select(ExecutorService.class).get();
            final var clientFactory = new VertxClientFactory(vertx, tracerFactory);
            return clientFactory.create(
                    connectionConfigurationMapper.map(dataSourceConfiguration.connection()),
                    serializer,
                    executorService);
        };
    }

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
        addJetstreamResources(DEFAULT_DATASOURCE, configuration.getValue());
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

    private @NonNull DataSourceConfiguration dataSourceConfiguration(@NonNull String datasource) {
        if (DEFAULT_DATASOURCE.equals(datasource)) {
            return configuration.getValue();
        }
        return Optional.ofNullable(configuration.getValue().namedDatasource().get(datasource))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Connection configuration not configured for datasource: " + datasource));
    }

    private @NonNull Client client(@NonNull String datasource) {
        final Instance<Client> clients = CDI.current().select(Client.class, Any.Literal.INSTANCE);
        return CDIUtils.getInstanceById(clients, datasource).get();
    }
}
