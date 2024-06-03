package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;

public class JetStreamReader implements AutoCloseable, ConnectionListener {
    private final static Logger logger = Logger.getLogger(JetStreamReader.class);

    private final JetStreamReaderConsumerConfiguration configuration;
    private final AtomicReference<JetStreamSubscription> subscription;

    public JetStreamReader(JetStreamReaderConsumerConfiguration configuration) {
        this.configuration = configuration;
        this.subscription = new AtomicReference<>();
    }

    public Message nextMessage(Supplier<Optional<Connection>> connection) {
        if (isActive()) {
            return getReader(connection).flatMap(this::nextMessage).orElse(null);
        }
        return null;
    }

    public boolean isActive() {
        return getSubscription().map(io.nats.client.Subscription::isActive).orElse(true);
    }

    private Optional<Message> nextMessage(io.nats.client.JetStreamReader reader) {
        try {
            return Optional.ofNullable(reader.nextMessage(configuration.maxRequestExpires().orElse(Duration.ZERO)));
        } catch (IllegalStateException e) {
            logger.debugf(e, "The subscription become inactive for stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.debugf(e, "The reader was interrupted for stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error reading next message from stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        }
    }

    @Override
    public void close() {
        getSubscription().ifPresent(this::close);
    }

    @Override
    public void onEvent(ConnectionEvent event, String message) {
        if (!ConnectionEvent.Connected.equals(event)) {
            subscription.set(null); // force resubscribe
        }
    }

    public void close(Subscription subscription) {
        try {
            if (subscription.isActive()) {
                subscription.drain(Duration.ofMillis(1000));
            }
        } catch (InterruptedException | IllegalStateException e) {
            logger.warnf("Interrupted while draining subscription");
        }
        try {
            if (subscription.isActive()) {
                subscription.unsubscribe();
            }
        } catch (IllegalStateException e) {
            logger.warnf("Failed to unsubscribe subscription");
        }
    }

    private Optional<JetStreamSubscription> getSubscription() {
        return Optional.ofNullable(subscription.get());
    }

    private synchronized Optional<io.nats.client.JetStreamReader> getReader(Supplier<Optional<Connection>> connection) {
        return connection.get().flatMap(this::getReader);
    }

    private synchronized Optional<io.nats.client.JetStreamReader> getReader(Connection connection) {
        if (connection.isConnected()) {
            final var subscription = getSubscription().orElseGet(() -> createSubscription(connection));
            return Optional.of(subscription.reader(configuration.maxRequestBatch(), configuration.rePullAt()));
        } else {
            return Optional.empty();
        }
    }

    private JetStreamSubscription createSubscription(Connection connection) {
        try {
            final var jetStream = connection.jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            this.subscription.set(jetStream.subscribe(configuration.subject(), optionsFactory.create(configuration)));
            return subscription.get();
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamReaderException(String.format("Failed to get subscription with message: %s", e.getMessage()), e);
        }
    }
}
