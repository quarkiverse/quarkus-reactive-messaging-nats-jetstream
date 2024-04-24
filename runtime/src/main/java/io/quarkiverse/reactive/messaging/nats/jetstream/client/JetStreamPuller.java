package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import io.nats.client.*;
import io.nats.client.impl.NatsJetStreamPullSubscription;
import org.jboss.logging.Logger;

public class JetStreamPuller {
    private final static Logger logger = Logger.getLogger(JetStreamPuller.class);

    private final JetStreamPullConsumerConfiguration configuration;
    private final PullSubscribeOptions pullSubscribeOptions;

    public JetStreamPuller(JetStreamPullConsumerConfiguration configuration) {
        this.configuration = configuration;
        final var optionsFactory = new PullSubscribeOptionsFactory();
        this.pullSubscribeOptions = optionsFactory.create(configuration);
    }

    public Optional<Message> nextMessage(Connection connection) {
        try {
            final var jetStream = connection.jetStream();
            final var subject = configuration.subject();
            return nextMessage((NatsJetStreamPullSubscription) jetStream.subscribe(subject, pullSubscribeOptions));
        } catch (IOException | JetStreamApiException e) {
            logger.errorf(e, "Failed to subscribe stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        }
    }

    private Optional<Message> nextMessage(NatsJetStreamPullSubscription subscription) {
        try {
            final var fetch = subscription.fetch(1, Duration.ofSeconds(10));
            return fetch.stream().findAny();
        } catch (IllegalStateException e) {
            logger.debugf(e, "The subscription become inactive for stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error reading next message from stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        } finally {
            close(subscription);
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
}
