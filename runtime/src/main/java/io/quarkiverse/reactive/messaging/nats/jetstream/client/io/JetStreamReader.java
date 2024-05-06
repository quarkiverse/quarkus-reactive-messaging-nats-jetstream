package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

import java.io.IOException;
import java.time.Duration;

import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamReaderConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullSubscribeOptionsFactory;

public class JetStreamReader implements AutoCloseable {
    private final static Logger logger = Logger.getLogger(JetStreamReader.class);

    private final JetStreamReaderConsumerConfiguration configuration;
    private final JetStreamSubscription subscription;
    private final io.nats.client.JetStreamReader reader;

    public JetStreamReader(Connection connection, JetStreamReaderConsumerConfiguration configuration)
            throws IOException, JetStreamApiException {
        this.configuration = configuration;
        final var jetStream = connection.jetStream();
        final var optionsFactory = new PullSubscribeOptionsFactory();
        subscription = jetStream.subscribe(configuration.subject(), optionsFactory.create(configuration));
        reader = subscription.reader(configuration.maxRequestBatch(), configuration.rePullAt());
    }

    public Message nextMessage() {
        if (isActive()) {
            try {
                return reader.nextMessage(configuration.maxRequestExpires().orElse(Duration.ZERO));
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
        return subscription.isActive();
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
