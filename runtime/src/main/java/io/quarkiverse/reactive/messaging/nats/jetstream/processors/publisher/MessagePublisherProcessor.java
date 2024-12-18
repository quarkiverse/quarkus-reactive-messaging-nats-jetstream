package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConnectionListener {
    private final AtomicReference<Status> readiness;
    private final AtomicReference<Status> liveness;
    private final AtomicReference<Connection<T>> connection;
    private final ConnectionFactory connectionFactory;
    private final ConnectionConfiguration connectionConfiguration;

    public MessagePublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration) {
        this.readiness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.liveness = new AtomicReference<>(
                Status.builder().event(ConnectionEvent.Closed).message("Publish processor inactive").healthy(false).build());
        this.connection = new AtomicReference<>();
        this.connectionFactory = connectionFactory;
        this.connectionConfiguration = connectionConfiguration;
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
    }

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe()
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage()))
                .onFailure().retry().withBackOff(retryBackoff()).indefinitely();
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        log.infof("Event: %s, message: %s, channel: %s", event, message, configuration().channel());
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

    protected abstract Multi<Message<T>> subscription(Connection<T> connection);

    protected abstract Duration retryBackoff();

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> recover(Throwable failure) {
        log.errorf(failure, "Failed to subscribe with message: %s", failure.getMessage());
        return subscribe();
    }

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe() {
        return getOrEstablishConnection()
                .onItem().transformToMulti(this::subscription)
                .onSubscription().invoke(() -> log.infof("Subscribed to channel %s", configuration().channel()));
    }

    private Uni<Connection<T>> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(this::connect)
                .onItem().invoke(this.connection::set);
    }

    private Uni<Connection<T>> connect() {
        return connectionFactory.create(connectionConfiguration, this);
    }

    private void close(Connection<T> connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception failure) {
            log.warnf(failure, "Failed to close resource with message: %s", failure.getMessage());
        }
    }
}
