package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.Optional;
import java.util.concurrent.Flow;
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
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class MessageSubscriberProcessor<T> implements MessageProcessor, ConnectionListener {
    private final ConnectionConfiguration connectionConfiguration;
    private final MessageSubscriberConfiguration configuration;
    private final ConnectionFactory connectionFactory;
    private final AtomicReference<Status> status;
    private final AtomicReference<Connection<T>> connection;

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

    public Flow.Subscriber<Message<T>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<T>> subscribe(Multi<Message<T>> subscription) {
        return subscription.onItem().transformToUniAndConcatenate(this::publish);
    }

    @Override
    public String channel() {
        return configuration.channel();
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
            log.warnf(failure, "Failed to close connection with message: %s", failure.getMessage());
        }
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        log.infof("Event: %s, message: %s, channel: %s", event, message, configuration.channel());
        this.status.set(Status.builder().healthy(true).message(message).event(event).build());
    }

    private Uni<Message<T>> publish(final Message<T> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> connection.publish(message, configuration))
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to publish with message: %s", failure.getMessage()))
                .onFailure().recoverWithUni(() -> recover(message));
    }

    private Uni<Message<T>> recover(final Message<T> message) {
        return Uni.createFrom().<Void> item(() -> {
            close();
            return null;
        })
                .onItem().transformToUni(v -> publish(message));
    }

    private Uni<? extends Connection<T>> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory.create(connectionConfiguration, this))
                .onItem().invoke(this.connection::set);
    }
}
