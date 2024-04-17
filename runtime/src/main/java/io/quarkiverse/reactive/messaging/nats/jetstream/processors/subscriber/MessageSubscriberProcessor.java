package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPublishException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPublisher;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.providers.helpers.MultiUtils;

public class MessageSubscriberProcessor implements MessageProcessor {
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
        this.status = new AtomicReference<>(new Status(true, "Not connected"));
    }

    public Flow.Subscriber<? extends Message<?>> getSubscriber() {
        return MultiUtils.via(m -> m.onSubscription()
                .call(this::getOrEstablishConnection)
                .onItem()
                .transformToUniAndConcatenate(this::send)
                .onCompletion().invoke(this::close)
                .onTermination().invoke(this::close)
                .onCancellation().invoke(this::close)
                .onFailure().invoke(throwable -> {
                    logger.errorf(throwable, "Failed to publish: %s", throwable.getMessage());
                    status.set(new Status(false, throwable.getMessage()));
                    close();
                }));
    }

    public Message<?> publish(final Connection connection, final Message<?> message) {
        try {
            return jetStreamPublisher.publish(connection, configuration, message);
        } catch (JetStreamPublishException e) {
            status.set(new Status(false, e.getMessage()));
            throw new MessageSubscriberException(String.format("Failed to process message: %s", e.getMessage()), e);
        }
    }

    /**
     * Connections are made only on first message dispatch for subscribers. To avoid health is reporting not ok
     * the method returns true if connection is not established.
     */
    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public void close() {
        jetStreamClient.close();
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    private Uni<? extends Message<?>> send(Message<?> message) {
        return getOrEstablishConnection()
                .onItem()
                .transformToUni(connection -> send(message, connection));
    }

    private Uni<Message<?>> send(Message<?> message, Connection connection) {
        return Uni.createFrom().<Message<?>> emitter(em -> {
            try {
                em.complete(publish(connection, message));
            } catch (Throwable e) {
                logger.errorf(e, "Failed sending message: %s", e.getMessage());
                em.fail(e);
            }
        })
                .emitOn(runnable -> connection.context().runOnContext(runnable))
                .onItem().transformToUni(this::acknowledge)
                .onFailure().recoverWithUni(throwable -> notAcknowledge(message, throwable));
    }

    private Uni<Message<?>> acknowledge(Message<?> message) {
        return Uni.createFrom().completionStage(message.ack())
                .onItem().transform(v -> message);
    }

    private Uni<Message<?>> notAcknowledge(Message<?> message, Throwable throwable) {
        return Uni.createFrom().completionStage(message.nack(throwable))
                .onItem().transform(v -> null);
    }

    private Uni<Connection> getOrEstablishConnection() {
        return jetStreamClient.getOrEstablishConnection()
                .onItem().invoke(() -> status.set(new Status(true, "Is connected")))
                .onFailure().invoke(throwable -> status.set(
                        new Status(false, "Connection failed with message: " + throwable.getMessage())));
    }
}
