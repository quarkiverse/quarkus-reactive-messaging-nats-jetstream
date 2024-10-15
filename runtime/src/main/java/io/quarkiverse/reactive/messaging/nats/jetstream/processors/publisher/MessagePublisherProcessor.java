package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConnectionListener {
    private final static Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    private final AtomicReference<Status> readiness;
    private final AtomicReference<Status> liveness;
    private final AtomicReference<Connection> connection;
    private final ConnectionFactory connectionFactory;
    private final ConnectionConfiguration connectionConfiguration;
    private final AtomicReference<Subscription<T>> subscription;

    public MessagePublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration) {
        this.readiness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.liveness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.connection = new AtomicReference<>();
        this.connectionFactory = connectionFactory;
        this.connectionConfiguration = connectionConfiguration;
        this.subscription = new AtomicReference<>();
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
    public void close() {
        close(this.connection.getAndSet(null));
        close(this.subscription.getAndSet(null));
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
            case Closed, CommunicationFailed, Disconnected ->
                this.readiness.set(Status.builder().event(event).message(message).healthy(false).build());
            case Reconnected ->
                this.readiness.set(Status.builder().event(event).message(message).healthy(true).build());
        }
    }

    protected abstract MessagePublisherConfiguration configuration();

    protected abstract Uni<Subscription<T>> subscription(Connection connection);

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> recover(Throwable failure) {
        return Uni.createFrom().<Void> item(() -> {
            close(this.subscription.getAndSet(null));
            return null;
        })
                .onItem().transformToMulti(v -> subscribe());
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe() {
        return getOrEstablishConnection()
                .onItem().transformToUni(this::subscription)
                .onItem().invoke(this.subscription::set)
                .onItem().transformToMulti(Subscription::subscribe)
                .onSubscription().invoke(() -> logger.infof("Subscribed to channel %s", configuration().channel()));
    }

    private Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(this::connect)
                .onItem().invoke(this.connection::set);
    }

    private Uni<? extends Connection> connect() {
        return connectionFactory.create(connectionConfiguration, this);
    }

    private void close(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Throwable failure) {
            logger.warnf(failure, "Failed to close resource with message: %s", failure.getMessage());
        }
    }
}
