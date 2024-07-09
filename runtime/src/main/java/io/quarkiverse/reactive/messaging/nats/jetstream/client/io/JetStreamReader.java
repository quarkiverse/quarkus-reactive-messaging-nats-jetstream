package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamStatusException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;

public class JetStreamReader implements AutoCloseable {
    private final static Logger logger = Logger.getLogger(JetStreamReader.class);

    private final JetStreamReaderConsumerConfiguration configuration;
    private final io.nats.client.JetStreamReader reader;
    private final JetStreamSubscription subscription;

    private JetStreamReader(JetStreamReaderConsumerConfiguration configuration, JetStreamSubscription subscription,
            io.nats.client.JetStreamReader reader) {
        this.configuration = configuration;
        this.subscription = subscription;
        this.reader = reader;
    }

    public static JetStreamReader of(Connection connection, JetStreamReaderConsumerConfiguration configuration) {
        try {
            final var jetStream = connection.jetStream();
            final var optionsFactory = new PullSubscribeOptionsFactory();
            final var subscription = jetStream.subscribe(configuration.subject(),
                    optionsFactory.create(configuration));
            final var reader = subscription.reader(configuration.maxRequestBatch(), configuration.rePullAt());
            return new JetStreamReader(configuration, subscription, reader);
        } catch (IOException | JetStreamApiException e) {
            throw new JetStreamReaderException(e);
        }
    }

    public boolean isActive() {
        return subscription.isActive();
    }

    public Optional<Message> nextMessage() {
        try {
            return Optional.ofNullable(reader.nextMessage(configuration.maxRequestExpires().orElse(Duration.ZERO)));
        } catch (JetStreamStatusException e) {
            logger.debugf(e, e.getMessage());
            return Optional.empty();
        } catch (IllegalStateException e) {
            logger.debugf(e, "The subscription become inactive for stream: %s and subject: %s",
                    configuration.consumerConfiguration().stream(), configuration.subject());
            return Optional.empty();
        } catch (InterruptedException e) {
            logger.debugf(e, "The reader was interrupted for stream: %s and subject: %s",
                    configuration.consumerConfiguration().stream(), configuration.subject());
            return Optional.empty();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error reading next message from stream: %s and subject: %s",
                    configuration.consumerConfiguration().stream(), configuration.subject());
            return Optional.empty();
        }
    }

    @Override
    public void close() {
        try {
            reader.stop();
        } catch (Exception e) {
            logger.warnf("Failed to stop reader with message %s", e.getMessage());
        }
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
            logger.warnf("Failed to unsubscribe subscription with message %s", e.getMessage());
        }
    }
}
