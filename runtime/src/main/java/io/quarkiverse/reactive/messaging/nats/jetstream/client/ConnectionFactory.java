package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class ConnectionFactory {
    private final ExecutionHolder executionHolder;
    private final MessageMapper messageMapper;
    private final JetStreamInstrumenter instrumenter;
    private final PayloadMapper payloadMapper;

    @Inject
    public ConnectionFactory(ExecutionHolder executionHolder,
            MessageMapper messageMapper,
            JetStreamInstrumenter instrumenter,
            PayloadMapper payloadMapper) {
        this.executionHolder = executionHolder;
        this.messageMapper = messageMapper;
        this.instrumenter = instrumenter;
        this.payloadMapper = payloadMapper;
    }

    public <T> Uni<? extends SubscribeConnection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            ReaderConsumerConfiguration<T> consumerConfiguration) {
        return getContext()
                .onItem()
                .transformToUni(context -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> new DefaultConnection(connectionConfiguration, connectionListener,
                                context, messageMapper, payloadMapper, instrumenter))))
                .onItem().transformToUni(connection -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> new ReaderSubscribeConnection<>(connection, consumerConfiguration))));
    }

    public <T> Uni<? extends SubscribeConnection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            PushConsumerConfiguration<T> consumerConfiguration) {

        return getContext()
                .onItem()
                .transformToUni(context -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> new DefaultConnection(connectionConfiguration, connectionListener,
                                context, messageMapper, payloadMapper, instrumenter))))
                .onItem().transformToUni(connection -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> new PushSubscribeConnection<>(connection, consumerConfiguration))));
    }

    public Uni<? extends Connection> create(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return getContext()
                .onItem().transformToUni(
                        context -> Uni.createFrom().item(Unchecked.supplier(() -> new DefaultConnection(connectionConfiguration,
                                connectionListener, context, messageMapper, payloadMapper, instrumenter))));

    }

    private Optional<Vertx> getVertx() {
        return Optional.ofNullable(executionHolder.vertx());
    }

    private Uni<Context> getContext() {
        return Uni.createFrom().item(Unchecked.supplier(
                () -> getVertx().map(Vertx::getOrCreateContext).orElseThrow(() -> new ContextException("No Vertx available"))));
    }
}
