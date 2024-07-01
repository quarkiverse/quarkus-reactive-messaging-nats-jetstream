package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.FetchConsumeOptions;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.StreamInfo;
import io.nats.client.api.StreamInfoOptions;
import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.ExponentialBackoff;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamPublishConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.JetStreamPublisher;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.setup.JetStreamSetup;
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
    private final JetStreamSetup jetStreamSetup;

    @Inject
    public JetStreamUtility(NatsConfiguration natsConfiguration, ExecutionHolder executionHolder, PayloadMapper payloadMapper,
            JetStreamInstrumenter jetStreamInstrumenter, MessageFactory messageFactory) {
        this.natsConfiguration = natsConfiguration;
        this.executionHolder = executionHolder;
        this.payloadMapper = payloadMapper;
        this.jetStreamInstrumenter = jetStreamInstrumenter;
        this.messageFactory = messageFactory;
        this.jetStreamSetup = new JetStreamSetup();
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

    public void addOrUpdateConsumer(Connection connection,
            ConsumerConfiguration configuration) {
        try {
            final var jsm = connection.jetStreamManagement();
            final var cc = io.nats.client.api.ConsumerConfiguration.builder()
                    .name(configuration.name())
                    .filterSubject(configuration.subject())
                    .build();
            jsm.addOrUpdateConsumer(configuration.stream(), cc);
        } catch (IOException | JetStreamApiException e) {
            throw new ConsumerException(e);
        }
    }

    public <T> Optional<Message<T>> nextMessage(Connection connection,
            Class<T> payloadType,
            ConsumerConfiguration configuration) {

        return nextMessage(connection, configuration).map(message -> messageFactory.create(
                message,
                configuration.traceEnabled(),
                payloadType,
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

    public List<PurgeResult> purgeAllStreams(Connection connection) {
        return getStreams(connection).stream().flatMap(streamName -> purgeStream(connection, streamName).stream()).toList();
    }

    private Optional<io.nats.client.Message> nextMessage(
            Connection connection,
            ConsumerConfiguration configuration) {
        try {
            final var streamContext = connection.getStreamContext(configuration.stream());
            final var consumerContext = streamContext.getConsumerContext(configuration.name());

            try (final var fetchConsumer = consumerContext.fetch(
                    FetchConsumeOptions.builder().maxMessages(1).noWait().build())) {
                final var message = fetchConsumer.nextMessage();
                return Optional.ofNullable(message);
            }
        } catch (Exception e) {
            logger.errorf(e, "Failed to fetch message from stream: %s and subject: %s",
                    configuration.stream(), configuration.subject());
            return Optional.empty();
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
