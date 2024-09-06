package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushSubscribeOptionsFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx.PushSubscribeMessageConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx.ReaderMessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
public class ConnectionFactory {
    private static final Logger logger = Logger.getLogger(ConnectionFactory.class);
    private final ExecutionHolder executionHolder;
    private final MessageFactory messageFactory;
    private final JetStreamInstrumenter instrumenter;

    @Inject
    public ConnectionFactory(ExecutionHolder executionHolder,
            MessageFactory messageFactory,
            JetStreamInstrumenter instrumenter) {
        this.executionHolder = executionHolder;
        this.messageFactory = messageFactory;
        this.instrumenter = instrumenter;
    }

    public <T> Uni<? extends MessageSubscribeConnection> subscribe(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            ReaderConsumerConfiguration<T> consumerConfiguration) {
        return getContext()
                .onFailure().invoke(failure -> logger.warn(failure.getMessage(), failure))
                .onItem().transformToUni(
                        context -> subscribe(connectionConfiguration, connectionListener, consumerConfiguration, context));
    }

    public <T> Uni<? extends MessageSubscribeConnection> subscribe(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            PushConsumerConfiguration<T> consumerConfiguration,
            PushSubscribeOptionsFactory optionsFactory) {
        return getContext()
                .onFailure().invoke(failure -> logger.warn(failure.getMessage(), failure))
                .onItem().transformToUni(context -> subscribe(connectionConfiguration, connectionListener,
                        consumerConfiguration, optionsFactory, context));
    }

    public Uni<? extends AdministrationConnection> administration(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return Uni.createFrom()
                .item(Unchecked.supplier(
                        () -> new io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx.AdministrationConnection(
                                connectionConfiguration,
                                connectionListener)));
    }

    public Uni<? extends MessageConnection> message(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        return getContext()
                .onFailure().invoke(failure -> logger.warn(failure.getMessage(), failure))
                .onItem().transformToUni(context -> message(connectionConfiguration, connectionListener, context));
    }

    private Optional<Vertx> getVertx() {
        return Optional.ofNullable(executionHolder.vertx());
    }

    private <T> Uni<? extends MessageSubscribeConnection> subscribe(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            ReaderConsumerConfiguration<T> consumerConfiguration,
            Context context) {
        return Uni.createFrom().item(Unchecked.supplier(() -> new ReaderMessageSubscribeConnection<>(connectionConfiguration,
                connectionListener, context, instrumenter, consumerConfiguration, messageFactory)))
                .emitOn(context::runOnContext);
    }

    private <T> Uni<? extends MessageSubscribeConnection> subscribe(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener,
            PushConsumerConfiguration<T> consumerConfiguration,
            PushSubscribeOptionsFactory optionsFactory,
            Context context) {
        return Uni.createFrom().item(Unchecked.supplier(() -> new PushSubscribeMessageConnection<>(connectionConfiguration,
                connectionListener, context, instrumenter, consumerConfiguration, messageFactory, optionsFactory)))
                .emitOn(context::runOnContext);
    }

    private Uni<Context> getContext() {
        return Uni.createFrom().item(Unchecked.supplier(
                () -> getVertx().map(Vertx::getOrCreateContext).orElseThrow(() -> new ContextException("No Vertx available"))));
    }

    private Uni<? extends MessageConnection> message(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener, Context context) {
        return Uni.createFrom()
                .item(Unchecked
                        .supplier(() -> new io.quarkiverse.reactive.messaging.nats.jetstream.client.vertx.MessageConnection(
                                connectionConfiguration,
                                connectionListener, messageFactory, context, instrumenter)))
                .emitOn(context::runOnContext);
    }
}
