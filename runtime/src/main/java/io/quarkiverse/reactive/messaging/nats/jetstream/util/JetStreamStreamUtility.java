package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import io.nats.client.*;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.impl.NatsJetStreamPullSubscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import jakarta.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetup;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetupConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

public class JetStreamStreamUtility {
    private static final Logger logger = Logger.getLogger(JetStreamStreamUtility.class);

    public <T> Message<T> publish(Message<T> message, RequestReplyConfiguration<T> configuration, Duration connectionTimeout) {
        try (JetStreamClient jetStreamClient = getJetStreamClient()) {
            try (Connection connection = jetStreamClient.getOrEstablishConnection().await().atMost(connectionTimeout)) {
                final var setup = new JetStreamSetup();
                final var setupResult = setup.addOrUpdateStream(connection, JetStreamSetupConfiguration.of(configuration));
                logger.debugf("Setup result: %s", setupResult);
                return publish(connection, message, configuration);
            }
        }
    }

    public <T> Optional<Message<T>> nextMessage(RequestReplyConfiguration<T> configuration) {
        try (JetStreamClient jetStreamClient = getJetStreamClient()) {
            try (Connection connection = jetStreamClient.getOrEstablishConnection().await()
                    .atMost(configuration.connectionTimeout())) {
                final var messageFactory = getMessageFactory();
                return nextMessage(configuration, connection).map(message -> messageFactory.create(
                        message,
                        configuration.traceEnabled(),
                        configuration.payloadType().orElse(null),
                        connection.context(),
                        new ExponentialBackoff(false, Duration.ZERO)));
            }
        }
    }

    public List<String> getStreams(Duration connectionTimeout) {
        try (JetStreamClient jetStreamClient = getJetStreamClient()) {
            try (Connection connection = jetStreamClient.getOrEstablishConnection().await().atMost(connectionTimeout)) {
                final var jsm = connection.jetStreamManagement();
                return jsm.getStreamNames();
            }
        } catch (IOException | JetStreamApiException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> Message<T> publish(Connection connection, Message<T> message, RequestReplyConfiguration<T> configuration) {
        final var jetStreamPublisher = getJetStreamPublisher();
        return jetStreamPublisher.publish(connection, new JetStreamPublishConfiguration() {
            @Override
            public boolean traceEnabled() {
                return configuration.traceEnabled();
            }

            @Override
            public String stream() {
                return configuration.stream();
            }

            @Override
            public String subject() {
                return configuration.subject();
            }
        }, message);
    }

    private <T> Optional<io.nats.client.Message> nextMessage(
            RequestReplyConfiguration<T> configuration,
            Connection connection) {
        try {
            final var optionsFactory = new PullSubscribeOptionsFactory();
            final var consumerConfiguration = optionsFactory
                    .create(configuration);

            final var jetStream = connection.jetStream();
            NatsJetStreamPullSubscription subscription = (NatsJetStreamPullSubscription) jetStream.subscribe(configuration.subject(), consumerConfiguration);
            connection.flush(Duration.ofSeconds(1));

            ConsumerInfo ci = subscription.getConsumerInfo();
            logger.infof("Server consumer is named -> %s", ci.getName());

            return nextMessage(configuration, subscription);
        } catch (Exception e) {
            logger.errorf(e, "Failed to fetch message from stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
        }
    }

    private <T> Optional<io.nats.client.Message> nextMessage(RequestReplyConfiguration<T> configuration, NatsJetStreamPullSubscription subscription) throws Exception {
        return subscription.fetch(1, configuration.maxRequestExpires().orElse(Duration.ZERO)).stream().findAny();
    }

    private JetStreamClient getJetStreamClient() {
        final var natsConfiguration = CDI.current().select(NatsConfiguration.class).get();
        final var executionHolder = CDI.current().select(ExecutionHolder.class).get();
        return new JetStreamClient(ConnectionConfiguration.of(natsConfiguration), executionHolder.vertx());
    }

    private JetStreamPublisher getJetStreamPublisher() {
        final var payloadMapper = CDI.current().select(PayloadMapper.class).get();
        final var jetStreamInstrumenter = CDI.current().select(JetStreamInstrumenter.class).get();
        return new JetStreamPublisher(payloadMapper, jetStreamInstrumenter);
    }

    private MessageFactory getMessageFactory() {
        return CDI.current().select(MessageFactory.class).get();
    }

}
