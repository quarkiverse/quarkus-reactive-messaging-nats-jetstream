package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ClientConfiguration.DEFAULT_DATASOURCE;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ClientConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.DataSourceConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkus.arc.BeanCreator;
import io.quarkus.arc.SyntheticCreationalContext;
import io.vertx.mutiny.core.Vertx;

/**
 * Creates the JetStream {@link Client} synthetic CDI bean of a datasource. One {@link Client} bean is registered per
 * configured datasource by {@code JetStreamProcessor}, which passes the datasource name as the
 * {@value #DATASOURCE_PARAM} parameter and declares every bean resolved here as an injection point.
 */
public class ClientBeanCreator implements BeanCreator<Client> {
    public static final String DATASOURCE_PARAM = "datasource";

    @Override
    public Client create(SyntheticCreationalContext<Client> context) {
        final var datasource = (String) context.getParams().get(DATASOURCE_PARAM);
        final var configuration = dataSourceConfiguration(context.getInjectedReference(ClientConfiguration.class),
                datasource);
        final var connectionConfigurationMapper = context.getInjectedReference(ConnectionConfigurationMapper.class);
        final var clientFactory = new VertxClientFactory(context.getInjectedReference(Vertx.class),
                context.getInjectedReference(TracerFactory.class));
        return clientFactory.create(
                connectionConfigurationMapper.map(configuration.connection()),
                configuration.threadPool(),
                context.getInjectedReference(Serializer.class),
                context.getInjectedReference(ExecutorService.class));
    }

    private @NonNull DataSourceConfiguration dataSourceConfiguration(@NonNull ClientConfiguration configuration,
            @NonNull String datasource) {
        if (DEFAULT_DATASOURCE.equals(datasource)) {
            return configuration;
        }
        return Optional.ofNullable(configuration.namedDatasource().get(datasource))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Connection configuration not configured for datasource: " + datasource));
    }
}
