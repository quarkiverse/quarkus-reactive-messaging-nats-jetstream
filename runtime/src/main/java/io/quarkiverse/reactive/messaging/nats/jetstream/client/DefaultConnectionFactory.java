package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class DefaultConnectionFactory implements ConnectionFactory {
    private final static Duration DEFAULT_BACKOFF = Duration.ofSeconds(1);

    private final ExecutionHolder executionHolder;
    private final MessageMapper messageMapper;
    private final PayloadMapper payloadMapper;
    private final ConsumerMapper consumerMapper;
    private final StreamStateMapper streamStateMapper;
    private final TracerFactory tracerFactory;

    @Override
    public <T> Uni<Connection<T>> create(final ConnectionConfiguration connectionConfiguration) {
        return create(connectionConfiguration, List.of());
    }

    @Override
    public <T> Uni<Connection<T>> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return create(connectionConfiguration, List.of(connectionListener));
    }

    public <T> Uni<Connection<T>> create(ConnectionConfiguration connectionConfiguration,
            List<ConnectionListener> connectionListeners) {
        final var vertx = getVertx();
        final var context = vertx.getOrCreateContext();
        if (connectionConfiguration.connectionAttempts().isEmpty()) {
            return context.<Connection<T>> executeBlocking(connect(connectionConfiguration, connectionListeners, vertx))
                    .onFailure().retry().withBackOff(connectionConfiguration.connectionBackoff().orElse(DEFAULT_BACKOFF))
                    .indefinitely();
        } else {
            return context.<Connection<T>> executeBlocking(connect(connectionConfiguration, connectionListeners, vertx))
                    .onFailure().retry().withBackOff(connectionConfiguration.connectionBackoff().orElse(DEFAULT_BACKOFF))
                    .atMost(connectionConfiguration.connectionAttempts().get());
        }
    }

    private <T> Uni<Connection<T>> connect(final ConnectionConfiguration connectionConfiguration,
            final List<ConnectionListener> connectionListeners,
            final Vertx vertx) {
        return Uni.createFrom().item(
                Unchecked.supplier(() -> new DefaultConnection<>(connectionConfiguration, connectionListeners,
                        messageMapper, payloadMapper, consumerMapper, streamStateMapper, tracerFactory, vertx)));
    }

    private Vertx getVertx() {
        return Optional.ofNullable(executionHolder.vertx()).orElseThrow(() -> new ContextException("No Vertx available"));
    }
}
