package io.quarkiverse.reactive.messaging.nats.jetstream;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.INCOMING;
import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.INCOMING_AND_OUTGOING;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import io.quarkiverse.reactive.messaging.nats.NatsConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePullPublisherProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePushPublisherProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.connector.OutboundConnector;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.health.HealthReporter;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import io.vertx.mutiny.core.Vertx;

@ApplicationScoped
@Connector(JetStreamConnector.CONNECTOR_NAME)

// Publish and subscriber processor attributes
@ConnectorAttribute(name = "stream", description = "The stream to subscribe or publish messages to", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "subject", description = "The subject to subscribe or publish messages to", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "trace-enabled", description = "Enable traces for publisher or subscriber", direction = INCOMING_AND_OUTGOING, type = "Boolean", defaultValue = "true")
@ConnectorAttribute(name = "auto-configure", description = "Auto configure subject on NATS", direction = INCOMING_AND_OUTGOING, type = "Boolean", defaultValue = "true")

// Publish processor attributes
@ConnectorAttribute(name = "ordered", description = "Flag indicating whether this subscription should be ordered", direction = INCOMING, type = "Boolean")
@ConnectorAttribute(name = "deliver-group", description = "The optional deliver group to join", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "durable", description = "Sets the durable name for the consumer", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "max-deliver", description = "The maximum number of times a specific message delivery will be attempted", direction = INCOMING, type = "Long", defaultValue = "1")
@ConnectorAttribute(name = "back-off", description = "The timing of re-deliveries as a comma-separated list of durations", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "payload-type", description = "The payload type", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "pull", description = "The subscription type", direction = INCOMING, type = "boolean", defaultValue = "true")
@ConnectorAttribute(name = "pull.batch-size", description = "The size of batch of messages to be pulled in pull mode", direction = INCOMING, type = "int", defaultValue = "100")
@ConnectorAttribute(name = "pull.repull-at", description = "The point in the current batch to tell the server to start the next batch", direction = INCOMING, type = "int", defaultValue = "50")
@ConnectorAttribute(name = "pull.poll-timeout", description = "The poll timeout in milliseconds, use 0 to wait indefinitely", direction = INCOMING, type = "int", defaultValue = "500")
@ConnectorAttribute(name = "retry-backoff", description = "The retry backoff in milliseconds for retry publishing messages", direction = INCOMING, type = "Long", defaultValue = "10000")
@ConnectorAttribute(name = "exponential-backoff", description = "Calculation a exponential backoff using deliveredCount metadata (NB back-off must undefined to work properly)", direction = INCOMING, type = "Boolean", defaultValue = "false")
@ConnectorAttribute(name = "exponential-backoff-max-duration", description = "The maximum duration of exponential backoff", direction = INCOMING, type = "String", defaultValue = "PT2M")
public class JetStreamConnector implements InboundConnector, OutboundConnector, HealthReporter {
    public static final String CONNECTOR_NAME = "quarkus-jetstream";

    private final List<MessageProcessor> processors;
    private final ExecutionHolder executionHolder;
    private final PayloadMapper payloadMapper;
    private final JetStreamInstrumenter jetStreamInstrumenter;
    private final NatsConfiguration natsConfiguration;

    @Inject
    public JetStreamConnector(PayloadMapper payloadMapper,
            JetStreamInstrumenter jetStreamInstrumenter,
            ExecutionHolder executionHolder,
            NatsConfiguration natsConfiguration) {
        this.payloadMapper = payloadMapper;
        this.jetStreamInstrumenter = jetStreamInstrumenter;
        this.processors = new CopyOnWriteArrayList<>();
        this.executionHolder = executionHolder;
        this.natsConfiguration = natsConfiguration;
    }

    @Override
    public Flow.Publisher<? extends Message<?>> getPublisher(Config config) {
        final var configuration = new JetStreamConnectorIncomingConfiguration(config);
        final var client = new JetStreamClient(ConnectionConfiguration.of(natsConfiguration), getVertx());
        final var processor = createMessagePublisherProcessor(client, configuration);
        processors.add(processor);
        return processor.getPublisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var configuration = new JetStreamConnectorIncomingConfiguration(config);
        final var client = new JetStreamClient(ConnectionConfiguration.of(natsConfiguration), getVertx());
        final var processor = new MessageSubscriberProcessor(client, MessageSubscriberConfiguration.of(configuration),
                payloadMapper, jetStreamInstrumenter);
        processors.add(processor);
        return processor.getSubscriber();
    }

    @Override
    public HealthReport getReadiness() {
        return getHealth();
    }

    @Override
    public HealthReport getLiveness() {
        return getHealth();
    }

    HealthReport getHealth() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        processors.forEach(client -> builder.add(new HealthReport.ChannelInfo(
                client.getChannel(),
                client.getStatus().healthy(),
                client.getStatus().message())));
        return builder.build();
    }

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
        this.processors.forEach(MessageProcessor::close);
    }

    public Vertx getVertx() {
        return executionHolder.vertx();
    }

    private MessagePublisherProcessor createMessagePublisherProcessor(JetStreamClient client,
            JetStreamConnectorIncomingConfiguration configuration) {
        if (configuration.getPull()) {
            return new MessagePullPublisherProcessor(client,
                    MessagePublisherConfiguration.of(configuration),
                    payloadMapper,
                    jetStreamInstrumenter);
        } else {
            return new MessagePushPublisherProcessor(client,
                    MessagePublisherConfiguration.of(configuration),
                    payloadMapper,
                    jetStreamInstrumenter);
        }
    }
}
