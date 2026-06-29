package io.quarkiverse.reactive.messaging.nats.jetstream.connector.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.NonNull;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class VertxClientFactory implements ClientFactory {
    private final ConnectorConfiguration configuration;
    private final ConnectionConfigurationMapper mapper;
    private final ExecutorService executorService;
    private final io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory clientFactory;

    public VertxClientFactory(ConnectorConfiguration configuration,
                              ConnectionConfigurationMapper mapper,
                              TracerFactory tracerFactory,
                              Vertx vertx,
                              ExecutorService executorService) {
        this.configuration = configuration;
        this.mapper = mapper;
        this.executorService = executorService;
        this.clientFactory = new io.quarkiverse.reactive.messaging.nats.jetstream.client.VertxClientFactory(vertx, tracerFactory);
    }

    @Override
    public @NonNull Uni<Client> create(@NonNull String datasource) {
        return clientFactory.create(mapper.map(configuration(datasource)), executorService);
    }

    @Override
    public @NonNull Uni<Client> create() {
        return clientFactory.create(mapper.map(configuration()), executorService);
    }

    private ConnectionConfiguration configuration(String datasource) {
        return Optional.ofNullable(configuration.datasource().named().get(datasource))
                .orElseThrow(() -> new IllegalArgumentException("Datasource not found: " + datasource));
    }

    private ConnectionConfiguration configuration() {
        return configuration.datasource();
    }
}
