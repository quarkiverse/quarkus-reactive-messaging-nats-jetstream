package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;

public class JetStreamPuller implements AutoCloseable {
    private final static Logger logger = Logger.getLogger(JetStreamPuller.class);

    private final JetStreamPullConsumerConfiguration configuration;
    private final JetStreamSubscription subscription;

    public JetStreamPuller(Connection connection, JetStreamPullConsumerConfiguration configuration)
            throws IOException, JetStreamApiException {
        this.configuration = configuration;
        final var jetStream = connection.jetStream();
        final var optionsFactory = new PullSubscribeOptionsFactory();
        subscription = jetStream.subscribe(configuration.subject(), optionsFactory.create(configuration));
    }

    public Optional<Message> nextMessage() {
        try {
            return Optional.ofNullable(subscription.nextMessage(configuration.pollTimeout()));
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
}
