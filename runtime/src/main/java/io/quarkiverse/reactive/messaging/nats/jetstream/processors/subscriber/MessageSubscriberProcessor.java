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
public class MessageSubscriberProcessor implements MessageProcessor, ConnectionListener {
    private final String channel;
    private final String stream;
    private final String subject;
    private final ConnectionConfiguration connectionConfiguration;
    private final ConnectionFactory connectionFactory;

    private final AtomicReference<Status> status;
    private final AtomicReference<Connection> connection;

    public MessageSubscriberProcessor(final String channel,
            final String stream,
            final String subject,
            final ConnectionConfiguration connectionConfiguration,
            final ConnectionFactory connectionFactory) {
        this.channel = channel;
        this.stream = stream;
        this.subject = subject;
        this.connectionConfiguration = connectionConfiguration;
        this.connectionFactory = connectionFactory;
        this.status = new AtomicReference<>(new Status(true, "Subscriber processor inactive", ConnectionEvent.Closed));
        this.connection = new AtomicReference<>();
    }

    public Flow.Subscriber<Message<?>> subscriber() {
        return MultiUtils.via(this::subscribe);
    }

    private Multi<Message<?>> subscribe(Multi<Message<?>> subscription) {
        return subscription.onItem().transformToUniAndConcatenate(this::publish);
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public String stream() {
        return stream;
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
        log.infof("Event: %s, message: %s, channel: %s", event, message, channel);
        this.status.set(Status.builder().healthy(true).message(message).event(event).build());
    }

    private Uni<Message<?>> publish(final Message<?> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> connection.publish(message, stream, subject))
                .onFailure()
                .invoke(failure -> log.errorf(failure, "Failed to publish with message: %s", failure.getMessage()))
                .onFailure().recoverWithUni(() -> recover(message));
    }

    private Uni<Message<?>> recover(final Message<?> message) {
        return Uni.createFrom().<Void> item(() -> {
            close();
            return null;
        })
                .onItem().transformToUni(v -> publish(message));
    }

    private Uni<? extends Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(() -> connectionFactory.create(connectionConfiguration, this))
                .onItem().invoke(this.connection::set);
    }
}
