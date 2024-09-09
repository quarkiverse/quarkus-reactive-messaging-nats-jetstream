package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageSubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class MessagePullPublisherProcessor implements MessagePublisherProcessor {
    private final static Logger logger = Logger.getLogger(MessagePullPublisherProcessor.class);

    private final MessagePullPublisherConfiguration<?> configuration;
    private final ConnectionFactory connectionFactory;
    private final AtomicReference<Status> status;
    private final AtomicReference<MessageSubscribeConnection> connection;
    private final ConnectionConfiguration connectionConfiguration;

    public MessagePullPublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final MessagePullPublisherConfiguration<?> configuration) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
        this.status = new AtomicReference<>(new Status(false, "Not connected", ConnectionEvent.Closed));
        this.connection = new AtomicReference<>();
        this.connectionConfiguration = connectionConfiguration;
    }

    @Override
    public Status getStatus() {
        return status.get();
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
        return configuration.channel();
    }

    @Override
    public Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publisher() {
        return getOrEstablishConnection()
                .onItem().transformToMulti(MessageSubscribeConnection::subscribe)
                .onFailure().invoke(throwable -> {
                    if (!isConsumerAlreadyInUse(throwable)) {
                        logger.errorf(throwable, "Failed to publish messages: %s", throwable.getMessage());
                        status.set(new Status(false, throwable.getMessage(), ConnectionEvent.CommunicationFailed));
                    }
                })
                .onFailure().retry().withBackOff(configuration.retryBackoff()).indefinitely();
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        switch (event) {
            case Connected -> this.status.set(new Status(true, message, event));
            case Closed -> this.status.set(new Status(false, message, event));
            case Disconnected -> this.status.set(new Status(false, message, event));
            case Reconnected -> this.status.set(new Status(true, message, event));
            case CommunicationFailed -> this.status.set(new Status(false, message, event));
        }
    }

    private Uni<MessageSubscribeConnection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory.subscribe(connectionConfiguration, this, configuration))
                .onItem().invoke(this.connection::set);
    }
}
