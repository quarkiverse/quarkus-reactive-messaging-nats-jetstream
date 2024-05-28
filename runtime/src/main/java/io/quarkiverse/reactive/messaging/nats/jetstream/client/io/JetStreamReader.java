package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;

public class JetStreamReader implements AutoCloseable, ConnectionListener {
    private final static Logger logger = Logger.getLogger(JetStreamReader.class);

    private final JetStreamReaderConsumerConfiguration configuration;
    private final AtomicReference<Connection> connection;
    private final AtomicReference<Subscription> subscription;

    public JetStreamReader(Connection connection, JetStreamReaderConsumerConfiguration configuration) {
        this.configuration = configuration;
        this.connection = new AtomicReference<>(connection);

        final var subscription = getSubscription(connection);
        final var reader = getReader(subscription);
        this.subscription = new AtomicReference<>(new Subscription(subscription, reader));
    }

    public Message nextMessage() {
        if (isActive()) {
            try {
                return getReader().nextMessage(configuration.maxRequestExpires().orElse(Duration.ZERO));
            } catch (IllegalStateException e) {
                logger.debugf(e, "The subscription become inactive for stream: %s and subject: %s",
                        configuration.stream(), configuration.subject());
            } catch (InterruptedException e) {
                logger.debugf(e, "The reader was interrupted for stream: %s and subject: %s",
                        configuration.stream(), configuration.subject());
            } catch (Throwable throwable) {
                logger.warnf(throwable, "Error reading next message from stream: %s and subject: %s",
                        configuration.stream(), configuration.subject());
            }
        }
        return null;
    }

    public boolean isActive() {
        return getConnection().map(Connection::isConnected).orElse(false) && getSubscription().isActive();
    }

    @Override
    public void close() {
        try {
            if (getSubscription().isActive()) {
                getSubscription().drain(Duration.ofMillis(1000));
            }
        } catch (InterruptedException | IllegalStateException e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (getSubscription().isActive()) {
                getSubscription().unsubscribe();
            }
        } catch (IllegalStateException e) {
            logger.warnf("Failed to unsubscribe subscription");
        }
    }

    @Override
    public void onEvent(ConnectionEvent event, Connection connection, String message) {
        this.connection.set(connection);
        if (ConnectionEvent.Reconnected.equals(event)) {
            final var subscription = getSubscription(connection);
            final var reader = getReader(subscription);
            this.subscription.set(new Subscription(subscription, reader));
        }
    }

    private Optional<Connection> getConnection() {
        return Optional.ofNullable(connection.get());
    }

    private io.nats.client.JetStreamReader getReader() {
        return subscription.get().reader();
    }

    private io.nats.client.JetStreamReader getReader(JetStreamSubscription subscription) {
        return subscription.reader(configuration.maxRequestBatch(), configuration.rePullAt());
    }

    private JetStreamSubscription getSubscription(Connection connection) {
        try {
            final var jetStream = connection.jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            return jetStream.subscribe(configuration.subject(), optionsFactory.create(configuration));
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamReaderException(String.format("Failed to get subscription with message: %s", e.getMessage()), e);
        }
    }

    private JetStreamSubscription getSubscription() {
        return subscription.get().subscription();
    }

    private record Subscription(JetStreamSubscription subscription, io.nats.client.JetStreamReader reader) {
    }
}
