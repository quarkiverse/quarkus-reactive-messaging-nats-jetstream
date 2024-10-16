package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Options.DEFAULT_MAX_RECONNECT;
import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrument;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class ConnectionFactory {
    private final static Logger logger = Logger.getLogger(ConnectionFactory.class);

    private final ExecutionHolder executionHolder;
    private final MessageMapper messageMapper;
    private final JetStreamInstrument instrumenter;
    private final PayloadMapper payloadMapper;
    private final ConsumerMapper consumerMapper;
    private final StreamStateMapper streamStateMapper;

    @Inject
    public ConnectionFactory(ExecutionHolder executionHolder,
            MessageMapper messageMapper,
            JetStreamInstrument instrumenter,
            PayloadMapper payloadMapper,
            ConsumerMapper consumerMapper,
            StreamStateMapper streamStateMapper) {
        this.executionHolder = executionHolder;
        this.messageMapper = messageMapper;
        this.instrumenter = instrumenter;
        this.payloadMapper = payloadMapper;
        this.consumerMapper = consumerMapper;
        this.streamStateMapper = streamStateMapper;
    }

    public Uni<? extends Connection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return getContext()
                .onItem().transformToUni(
                        context -> Uni.createFrom().item(Unchecked.supplier(() -> new DefaultConnection(connectionConfiguration,
                                connectionListener, context, messageMapper, payloadMapper, consumerMapper, streamStateMapper,
                                instrumenter))))
                .onFailure().invoke(failure -> logger.errorf(failure, "Failed connecting to NATS: %s", failure.getMessage()))
                .onFailure()
                .retry()
                .withBackOff(connectionConfiguration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT))
                .atMost(connectionConfiguration.connectionAttempts().orElse(DEFAULT_MAX_RECONNECT));
    }

    private Optional<Vertx> getVertx() {
        return Optional.ofNullable(executionHolder.vertx());
    }

    private Uni<Context> getContext() {
        return Uni.createFrom().item(Unchecked.supplier(
                () -> getVertx().map(Vertx::getOrCreateContext).orElseThrow(() -> new ContextException("No Vertx available"))));
    }
}
