package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public abstract class MessagePublisherProcessor<T> implements MessageProcessor, ConnectionListener {
    private final static Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    private final static int CONSUMER_ALREADY_IN_USE = 10013;

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

    public Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> publisher() {
        return subscribe()
                .onFailure().transform(this::transformFailure)
                .onFailure().retry().withBackOff(configuration().retryBackoff()).indefinitely();
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
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

    protected abstract Uni<? extends SubscribeConnection<T>> connect();

    private Multi<org.eclipse.microprofile.reactive.messaging.Message<T>> subscribe() {
        return getOrEstablishConnection()
                .onItem().transformToMulti(SubscribeConnection::subscribe);
    }

    private Uni<SubscribeConnection<T>> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(Connection::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(this::connect)
                .onItem().invoke(this.connection::set);
    }

    private SubscribeException transformFailure(final Throwable failure) {
        if (isCommunicationFailure(failure) && !isConsumerAlreadyInUse(failure)) {
            logger.errorf(failure, "Failed to publish messages: %s", failure.getMessage());
            Optional.ofNullable(this.connection.get())
                    .ifPresent(connection -> connection.fireEvent(ConnectionEvent.CommunicationFailed, failure.getMessage()));
            close();
        }
        return new SubscribeException(failure);
    }

    private boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException jetStreamApiException) {
            return jetStreamApiException.getApiErrorCode() == CONSUMER_ALREADY_IN_USE;
        }
        return false;
    }

    private boolean isCommunicationFailure(Throwable failure) {
        return failure instanceof JetStreamApiException || failure instanceof IOException;
    }
}
