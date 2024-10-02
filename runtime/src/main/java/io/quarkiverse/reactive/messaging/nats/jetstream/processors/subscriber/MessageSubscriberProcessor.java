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
import io.smallrye.mutiny.Uni;
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
        this.status = new AtomicReference<>(new Status(true, "Connection closed", ConnectionEvent.Closed));
        this.connection = new AtomicReference<>();
    }

    public Flow.Subscriber<? extends Message<?>> subscriber() {
        return MultiUtils.via(m -> m.onSubscription()
                .call(this::getOrEstablishConnection)
                .onItem()
                .transformToUniAndConcatenate(this::publish)
                .onFailure()
                .invoke(throwable -> connection.get().fireEvent(ConnectionEvent.CommunicationFailed, throwable.getMessage())));

    }

    @Override
    public Status getStatus() {
        return this.status.get();
    }

    @Override
    public void close() {
        try {
            final var connection = this.connection.get();
            if (connection != null) {
                connection.close();
            }
        } catch (Throwable failure) {
            logger.warnf(failure, "Failed to close connection", failure);
        }
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        switch (event) {
            case Connected -> this.status.set(new Status(true, message, event));
            case Closed -> this.status.set(new Status(true, message, event));
            case Disconnected -> this.status.set(new Status(false, message, event));
            case Reconnected -> this.status.set(new Status(true, message, event));
            case CommunicationFailed -> this.status.set(new Status(false, message, event));
        }
    }

    private Uni<? extends Message<?>> publish(Message<?> message) {
        return getOrEstablishConnection()
                .onItem().transformToUni(connection -> connection.publish(message, configuration));
    }

    private Uni<? extends Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory.create(connectionConfiguration, this))
                .onItem().invoke(this.connection::set);
    }
}
