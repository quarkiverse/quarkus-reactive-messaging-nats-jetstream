package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.JetStreamPublisher;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;

public class MessageSubscriberProcessor implements MessageProcessor, ConnectionListener {
    private final static Logger logger = Logger.getLogger(MessageSubscriberProcessor.class);

    private final MessageSubscriberConfiguration configuration;
    private final JetStreamClient jetStreamClient;
    private final JetStreamPublisher jetStreamPublisher;
    private final AtomicReference<Status> status;

    public MessageSubscriberProcessor(
            final JetStreamClient jetStreamClient,
            final MessageSubscriberConfiguration configuration,
            final JetStreamPublisher jetStreamPublisher) {
        this.jetStreamClient = jetStreamClient;
        this.configuration = configuration;
        this.jetStreamPublisher = jetStreamPublisher;
        this.status = new AtomicReference<>(new Status(true, "Connection closed", ConnectionEvent.Closed));
    }

    public Flow.Subscriber<? extends Message<?>> subscriber() {
        return MultiUtils.via(m -> m.onSubscription()
                .call(this::getOrEstablishConnection)
                .onItem()
                .transformToUniAndConcatenate(this::publish)
                .onFailure()
                .invoke(throwable -> jetStreamClient.fireEvent(ConnectionEvent.CommunicationFailed, throwable.getMessage())));

    }

    @Override
    public Status getStatus() {
        return this.status.get();
    }

    @Override
    public void close() {
        jetStreamClient.close();
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
                .onItem()
                .transformToUni(connection -> publish(message, connection));
    }

    public Uni<Message<?>> publish(Message<?> message, Connection connection) {
        return Uni.createFrom().item(() -> jetStreamPublisher.publish(connection, configuration, message))
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    private Uni<Message<?>> acknowledge(Message<?> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message<?>> notAcknowledge(Message<?> message, Throwable throwable) {
        logger.errorf(throwable, "Failed to publish: %s", message);
        status.set(new Status(false, "Failed to publish message", ConnectionEvent.CommunicationFailed));
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().transform(v -> null);
    }

    private Uni<Connection> getOrEstablishConnection() {
        return jetStreamClient.getOrEstablishConnection();
    }
}
