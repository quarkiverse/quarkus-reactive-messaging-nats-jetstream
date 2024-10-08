package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.Optional;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;

public class MessageSubscriberProcessor implements MessageProcessor, ConnectionListener {
    private final static Logger logger = Logger.getLogger(MessageSubscriberProcessor.class);

    private final ConnectionConfiguration connectionConfiguration;
    private final MessageSubscriberConfiguration configuration;
    private final ConnectionFactory connectionFactory;
    private final AtomicReference<Status> status;
    private final AtomicReference<Connection> connection;

    public MessageSubscriberProcessor(
            final ConnectionConfiguration connectionConfiguration,
            final ConnectionFactory connectionFactory,
            final MessageSubscriberConfiguration configuration) {
        this.connectionConfiguration = connectionConfiguration;
        this.connectionFactory = connectionFactory;
        this.configuration = configuration;
        this.status = new AtomicReference<>(new Status(true, "Subscriber processor inactive", ConnectionEvent.Closed));
        this.connection = new AtomicReference<>();
    }

    public Flow.Subscriber<? extends Message<?>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<? extends Message<?>> subscribe(Multi<? extends Message<?>> subscription) {
        return subscription.onItem().transformToUniAndConcatenate(this::publish);
    }

    @Override
    public String channel() {
        return configuration.getChannel();
    }

    @Override
    public Status readiness() {
        return status.get();
    }

    @Override
    public Status liveness() {
        return status.get();
    }

    @Override
    public void close() {
        try {
            final var connection = this.connection.getAndSet(null);
            if (connection != null) {
                connection.close();
            }
        } catch (Throwable failure) {
            logger.warnf(failure, "Failed to close connection", failure);
        }
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        this.status.set(Status.builder().healthy(true).message(message).event(event).build());
    }

    private <T> Uni<Message<T>> publish(final Message<T> message) {
        return getOrEstablishConnection()
                .onItem().transformToUni(connection -> connection.publish(message, configuration))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(failure -> recover(message, failure));
    }

    private <T> Uni<Message<T>> recover(final Message<T> message, final Throwable failure) {
        return notAcknowledge(message, failure)
                .onItem().transformToUni(this::closeConnection)
                .onFailure().recoverWithUni(() -> closeConnection(message));
    }

    private <T> Uni<Message<T>> closeConnection(final Message<T> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            close();
            return message;
        }));
    }

    private Uni<? extends Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory.create(connectionConfiguration, this))
                .onItem().invoke(this.connection::set);
    }

    private <T> Uni<Message<T>> acknowledge(final Message<T> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private <T> Uni<Message<T>> notAcknowledge(final Message<T> message, final Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().invoke(() -> logger.warnf(throwable, "Message not published: %s", throwable.getMessage()))
                .onItem().transformToUni(v -> Uni.createFrom().item(message));
    }
}
