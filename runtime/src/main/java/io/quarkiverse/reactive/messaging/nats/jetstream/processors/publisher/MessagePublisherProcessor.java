package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConnectionListener {
    private final static Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    private final AtomicReference<Status> readiness;
    private final AtomicReference<Status> liveness;
    private final AtomicReference<SubscribeConnection<T>> connection;

    public MessagePublisherProcessor() {
        this.readiness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.liveness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.connection = new AtomicReference<>();
    }

    @Override
    public String channel() {
        return configuration().channel();
    }

    @Override
    public Status readiness() {
        return readiness.get();
    }

    @Override
    public Status liveness() {
        return liveness.get();
    }

    @Override
    public AtomicReference<? extends Connection> connection() {
        return connection;
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe()
                .onFailure()
                .invoke(failure -> logger.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage()))
                .onFailure().recoverWithMulti(this::recover);

    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        logger.infof("Event: %s, message: %s, channel: %s", event, message, configuration().channel());
        switch (event) {
            case Connected -> {
                this.readiness.set(Status.builder().event(event).message(message).healthy(true).build());
                this.liveness.set(Status.builder().event(event).message(message).healthy(true).build());
            }
            case Closed, CommunicationFailed, Disconnected, SubscriptionInactive ->
                this.readiness.set(Status.builder().event(event).message(message).healthy(false).build());
            case Reconnected ->
                this.readiness.set(Status.builder().event(event).message(message).healthy(true).build());
        }
    }

    protected abstract MessagePublisherConfiguration configuration();

    protected abstract Uni<? extends SubscribeConnection<T>> connect();

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> recover(Throwable failure) {
        return close().onItem().transformToMulti(v -> subscribe());
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe() {
        return getOrEstablishConnection()
                .onItem().transformToMulti(SubscribeConnection::subscribe)
                .onSubscription().invoke(() -> logger.infof("Subscribed to channel %s", configuration().channel()));
    }

    private Uni<SubscribeConnection<T>> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(this::connect)
                .onItem().invoke(this.connection::set);
    }
}
