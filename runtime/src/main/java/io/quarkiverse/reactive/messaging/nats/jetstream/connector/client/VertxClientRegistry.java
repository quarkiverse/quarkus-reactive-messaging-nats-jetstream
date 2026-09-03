package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.DataSourceConfiguration;
import io.vertx.mutiny.core.Vertx;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@ApplicationScoped
public class VertxClientRegistry implements ClientRegistry {
    private static final String DEFAULT_CLIENT_NAME = "default";
    private final ConnectorConfiguration configuration;
    private final ConnectionConfigurationMapper connectionConfigurationMapper;
    private final ExecutorService executorService;
    private final ClientFactory clientFactory;
    private final Serializer serializer;

    private final ConcurrentMap<String, Client> clients = new ConcurrentHashMap<>();
    private final AtomicBoolean shuttingDown = new AtomicBoolean();

    VertxClientRegistry(ConnectorConfiguration configuration,
            TracerFactory tracerFactory,
            ConnectionConfigurationMapper connectionConfigurationMapper,
            Serializer serializer,
            Vertx vertx,
            ExecutorService executorService) {
        this.configuration = configuration;
        this.connectionConfigurationMapper = connectionConfigurationMapper;

        this.serializer = serializer;
        this.executorService = executorService;
        this.clientFactory = new io.quarkiverse.reactive.messaging.nats.jetstream.client.VertxClientFactory(vertx,
                tracerFactory);
    }

    @Override
    public @NonNull Client lookup(@NonNull final String datasource) {
        if (shuttingDown.get()) {
            throw new IllegalStateException("Connection registry is shutting down");
        }
        return clients.computeIfAbsent(datasource, this::create);
    }

    private ConnectionConfiguration configuration(final String datasource) {
        if (DEFAULT_CLIENT_NAME.equals(datasource)) {
            return configuration(configuration);
        } else {
            return Optional.ofNullable(configuration.namedDatasource().get(datasource))
                    .map(this::configuration)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Connection configuration not configured for datasource: " + datasource));
        }
    }

    private @NonNull Client create(@NonNull String datasource) {
        return clientFactory.create(configuration(datasource), serializer, executorService);
    }

    private @NonNull ConnectionConfiguration configuration(@NonNull DataSourceConfiguration configuration) {
        return connectionConfigurationMapper.map(configuration.connection());
    }

    @PreDestroy
    void close() {
        shuttingDown.set(true);
        clients.forEach((name, client) -> {
            try {
                client.close();
            } catch (Exception e) {
                log.error("Could not close client '" + name + "': " + e.getMessage());
            }
        });
        clients.clear();
    }
}
