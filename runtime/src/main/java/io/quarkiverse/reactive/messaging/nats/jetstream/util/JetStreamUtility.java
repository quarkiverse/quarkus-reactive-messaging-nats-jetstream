package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.ConsumerContext;
import io.nats.client.FetchConsumeOptions;
import io.nats.client.FetchConsumer;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.ConsumerInfo;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamPublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetstreamConsumerConfigurtationFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.JetStreamPublisher;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

@ApplicationScoped
public class JetStreamUtility {
    private static final Logger logger = Logger.getLogger(JetStreamUtility.class);

    private final NatsConfiguration natsConfiguration;
    private final ExecutionHolder executionHolder;
    private final PayloadMapper payloadMapper;
    private final JetStreamInstrumenter jetStreamInstrumenter;
    private final MessageFactory messageFactory;

    @Inject
    public JetStreamUtility(NatsConfiguration natsConfiguration, ExecutionHolder executionHolder, PayloadMapper payloadMapper,
            JetStreamInstrumenter jetStreamInstrumenter, MessageFactory messageFactory) {
        this.natsConfiguration = natsConfiguration;
        this.executionHolder = executionHolder;
        this.payloadMapper = payloadMapper;
        this.jetStreamInstrumenter = jetStreamInstrumenter;
        this.messageFactory = messageFactory;
    }

    public JetStreamClient getJetStreamClient() {
        return new JetStreamClient(ConnectionConfiguration.of(natsConfiguration), executionHolder.vertx());
    }

    public Connection getConnection(JetStreamClient jetStreamClient, Duration connectionTimeout) {
        return jetStreamClient.getOrEstablishConnection().await().atMost(connectionTimeout);
    }

    public <T> Message<T> publish(Connection connection,
            Message<T> message,
            JetStreamPublishConfiguration configuration) {
        final var jetStreamPublisher = getJetStreamPublisher();
        return jetStreamPublisher.publish(connection, configuration, message);
    }

    public ConsumerInfo getConsumerInfo(Connection connection, String stream, String consumerName) {
        try {
            final var jsm = connection.jetStreamManagement();
            return jsm.getConsumerInfo(stream, consumerName);
        } catch (IOException | JetStreamApiException e) {
            throw new ConsumerException(e);
        }
    }

    private ConsumerContext getConsumerContext(Connection connection, String stream, String consumerName) {
        try {
            final var streamContext = connection.getStreamContext(stream);
            return streamContext.getConsumerContext(consumerName);
        } catch (IOException | JetStreamApiException e) {
            throw new ConsumerException(e);
        }
    }

    public <T> ConsumerContext addOrUpdateConsumer(Connection connection,
            ConsumerConfiguration<T> configuration) {
        try {
            final var factory = new JetstreamConsumerConfigurtationFactory();
            final var consumerConfiguration = factory.create(configuration);
            final var streamContext = connection.getStreamContext(configuration.stream());
            final var consumerContext = streamContext.createOrUpdateConsumer(consumerConfiguration);
            connection.flush(Duration.ZERO);
            return consumerContext;
        } catch (IOException | JetStreamApiException e) {
            throw new ConsumerException(e);
        }
    }

    public <T> Optional<Message<T>> nextMessage(Connection connection,
            ConsumerConfiguration<T> configuration) {
        return nextMessage(connection,
                getConsumerContext(connection, configuration.stream(),
                        configuration.name()
                                .orElseThrow(() -> new IllegalArgumentException("Consumer name is not configured"))),
                configuration);

    }

    public <T> Optional<Message<T>> nextMessage(Connection connection,
            ConsumerContext consumerContext,
            ConsumerConfiguration<T> configuration) {
        return nextMessage(consumerContext, configuration.fetchTimeout().orElse(null)).map(message -> messageFactory.create(
                message,
                configuration.traceEnabled(),
                configuration.getPayloadType().orElse(null),
                connection.context(),
                new ExponentialBackoff(false, Duration.ZERO),
                configuration.ackTimeout().orElseGet(() -> Duration.ofSeconds(10))));
    }

    public List<String> getStreams(Connection connection) {
        try {
            final var jsm = connection.jetStreamManagement();
            return jsm.getStreamNames();
        } catch (IOException | JetStreamApiException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getSubjects(Connection connection, String streamName) {
        return getStreamInfo(connection, streamName).map(streamInfo -> streamInfo.getConfiguration()
                .getSubjects()).orElseGet(List::of);
    }

    public List<String> getConsumerNames(Connection connection, String streamName) {
        try {
            final var jsm = connection.jetStreamManagement();
            return jsm.getConsumerNames(streamName);
        } catch (IOException | JetStreamApiException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<PurgeResult> purgeStream(Connection connection, String streamName) {
        try {
            final var jsm = connection.jetStreamManagement();
            final var response = jsm.purgeStream(streamName);
            return Optional.of(new PurgeResult(streamName, response.isSuccess(), response.getPurged()));
        } catch (IOException | JetStreamApiException e) {
            logger.debugf(e, "Unable to purge stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Deletes a message, overwriting the message data with garbage
     * This can be considered an expensive (time-consuming) operation, but is more secure.
     *
     * @param connection connection to NATS
     * @param stream name of the stream
     * @param sequence the sequence number of the message
     * @param erase whether to erase the message (overwriting with garbage) or only mark it as erased.
     * @throws DeleteException when message is not deleted
     */
    public void deleteMessage(Connection connection, String stream, long sequence, boolean erase) {
        try {
            final var jsm = connection.jetStreamManagement();
            if (!jsm.deleteMessage(stream, sequence, erase)) {
                throw new DeleteException(
                        String.format("Unable to delete message in stream %s with sequence %d", stream, sequence));
            }
        } catch (IOException | JetStreamApiException e) {
            throw new DeleteException(String.format("Unable to delete message in stream %s with sequence %d: %s", stream,
                    sequence, e.getMessage()), e);
        }
    }

    public List<PurgeResult> purgeAllStreams(Connection connection) {
        return getStreams(connection).stream().flatMap(streamName -> purgeStream(connection, streamName).stream()).toList();
    }

    public Optional<StreamState> getStreamState(Connection connection, String streamName) {
        return getStreamInfo(connection, streamName).map(streamInfo -> StreamState.of(streamInfo.getStreamState()));
    }

    private Optional<io.nats.client.Message> nextMessage(ConsumerContext consumerContext, Duration timeout) {
        try {
            try (final var fetchConsumer = fetchConsumer(consumerContext, timeout)) {
                final var message = fetchConsumer.nextMessage();
                return Optional.ofNullable(message);
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to fetch message: %s", e.getMessage());
            return Optional.empty();
        }
    }

    private FetchConsumer fetchConsumer(ConsumerContext consumerContext, Duration timeout)
            throws IOException, JetStreamApiException {
        if (timeout == null) {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).noWait().build());
        } else {
            return consumerContext.fetch(FetchConsumeOptions.builder().maxMessages(1).expiresIn(timeout.toMillis()).build());
        }
    }

    private JetStreamPublisher getJetStreamPublisher() {
        return new JetStreamPublisher(payloadMapper, jetStreamInstrumenter);
    }

    private Optional<StreamInfo> getStreamInfo(Connection connection, String streamName) {
        try {
            final var jsm = connection.jetStreamManagement();
            return Optional.of(jsm.getStreamInfo(streamName, StreamInfoOptions.allSubjects()));
        } catch (IOException | JetStreamApiException e) {
            logger.debugf(e, "Unable to read stream %s with message: %s", streamName, e.getMessage());
            return Optional.empty();
        }
    }

}
