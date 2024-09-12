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
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerType;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberProcessor;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.connector.OutboundConnector;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.health.HealthReporter;

@ApplicationScoped
@Connector(JetStreamConnector.CONNECTOR_NAME)

// Publish and subscriber processor attributes
@ConnectorAttribute(name = "stream", description = "The stream to subscribe or publish messages to", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "subject", description = "The subject to subscribe or publish messages to", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "trace-enabled", description = "Enable traces for publisher or subscriber", direction = INCOMING_AND_OUTGOING, type = "Boolean", defaultValue = "true")

// Publish common processor attributes
@ConnectorAttribute(name = "name", description = "The name of the NATS consumer", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "publisher-type", description = "The publisher type (Pull, Push)", direction = INCOMING, type = "String", defaultValue = "Pull")
@ConnectorAttribute(name = "payload-type", description = "The payload type", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "durable", description = "Sets the durable name for the consumer", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "filter-subjects", description = "A comma separated list of subjects that overlap with the subjects bound to the stream to filter delivery to subscribers", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "ack-wait", description = "The duration that the server will wait for an ack for any individual message once it has been delivered to a consumer. If an ack is not received in time, the message will be redelivered.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "deliver-policy", description = "The point in the stream to receive messages from, either DeliverAll, DeliverLast, DeliverNew, DeliverByStartSequence, DeliverByStartTime, or DeliverLastPerSubject.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "description", description = "A description of the consumer.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "inactive-threshold", description = "Duration that instructs the server to cleanup consumers that are inactive for that long.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "max-ack-pending", description = "Defines the maximum number of messages, without an acknowledgement, that can be outstanding.", direction = INCOMING, type = "Integer")
@ConnectorAttribute(name = "max-deliver", description = "The maximum number of times a specific message delivery will be attempted", direction = INCOMING, type = "Integer")
@ConnectorAttribute(name = "replay-policy", description = "If the policy is ReplayOriginal, the messages in the stream will be pushed to the client at the same rate that they were originally received, simulating the original timing of messages. If the policy is ReplayInstant (the default), the messages will be pushed to the client as fast as possible while adhering to the Ack Policy, Max Ack Pending and the client's ability to consume those messages.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "replicas", description = "Sets the number of replicas for the consumer's state. By default, when the value is set to zero, consumers inherit the number of replicas from the stream.", direction = INCOMING, type = "Integer")
@ConnectorAttribute(name = "memory-storage", description = "If set, forces the consumer state to be kept in memory rather than inherit the storage type of the stream (file in this case).", direction = INCOMING, type = "Boolean")
@ConnectorAttribute(name = "back-off", description = "The timing of re-deliveries as a comma-separated list of durations", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "retry-backoff", description = "The retry backoff in milliseconds for retry publishing messages", direction = INCOMING, type = "Long", defaultValue = "10000")
@ConnectorAttribute(name = "exponential-backoff", description = "Calculation a exponential backoff using deliveredCount metadata (NB back-off must undefined to work properly)", direction = INCOMING, type = "Boolean", defaultValue = "false")
@ConnectorAttribute(name = "exponential-backoff-max-duration", description = "The maximum duration of exponential backoff", direction = INCOMING, type = "String", defaultValue = "PT2M")
@ConnectorAttribute(name = "ack-timeout", description = "The duration to wait for an ack confirmation", direction = INCOMING, type = "String", defaultValue = "PT2S")

// Publish pull processor attributes
@ConnectorAttribute(name = "pull.batch-size", description = "The size of batch of messages to be pulled in pull mode", direction = INCOMING, type = "int", defaultValue = "100")
@ConnectorAttribute(name = "pull.repull-at", description = "The point in the current batch to tell the server to start the next batch", direction = INCOMING, type = "int", defaultValue = "50")
@ConnectorAttribute(name = "pull.max-waiting", description = "The maximum number of waiting pull requests.", direction = INCOMING, type = "Integer")
@ConnectorAttribute(name = "pull.max-expires", description = "The maximum duration a single pull request will wait for messages to be available to pull.", direction = INCOMING, type = "String")

// Publish push processor attributes
@ConnectorAttribute(name = "push.ordered", description = "Flag indicating whether this subscription should be ordered", direction = INCOMING, type = "Boolean")
@ConnectorAttribute(name = "push.deliver-group", description = "The optional deliver group to join", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "push.flow-control", description = "Enables per-subscription flow control using a sliding-window protocol. This protocol relies on the server and client exchanging messages to regulate when and how many messages are pushed to the client. This one-to-one flow control mechanism works in tandem with the one-to-many flow control imposed by MaxAckPending across all subscriptions bound to a consumer.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "push.idle-heart-beat", description = "If the idle heartbeat period is set, the server will regularly send a status message to the client (i.e. when the period has elapsed) while there are no new messages to send. This lets the client know that the JetStream service is still up and running, even when there is no activity on the stream. The message status header will have a code of 100. Unlike FlowControl, it will have no reply to address. It may have a description such \"Idle Heartbeat\". Note that this heartbeat mechanism is all handled transparently by supported clients and does not need to be handled by the application.", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "push.rate-limit", description = "Used to throttle the delivery of messages to the consumer, in bits per second.", direction = INCOMING, type = "Long")
@ConnectorAttribute(name = "push.headers-only", description = "Delivers only the headers of messages in the stream and not the bodies. Additionally adds Nats-Msg-Size header to indicate the size of the removed payload.", direction = INCOMING, type = "Boolean")
public class JetStreamConnector implements InboundConnector, OutboundConnector, HealthReporter {
    public static final String CONNECTOR_NAME = "quarkus-jetstream";

    private final List<MessageProcessor> processors;
    private final NatsConfiguration natsConfiguration;
    private final ConnectionFactory connectionFactory;

    @Inject
    public JetStreamConnector(
            NatsConfiguration natsConfiguration,
            ConnectionFactory connectionFactory) {
        this.processors = new CopyOnWriteArrayList<>();
        this.natsConfiguration = natsConfiguration;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Flow.Publisher<? extends Message<?>> getPublisher(Config config) {
        final var configuration = new JetStreamConnectorIncomingConfiguration(config);
        final var processor = createMessagePublisherProcessor(configuration);
        processors.add(processor);
        return processor.publisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration);
        final var configuration = new JetStreamConnectorIncomingConfiguration(config);
        final var processor = new MessageSubscriberProcessor(
                connectionConfiguration,
                connectionFactory,
                MessageSubscriberConfiguration.of(configuration));
        processors.add(processor);
        return processor.subscriber();
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

    private MessagePublisherProcessor createMessagePublisherProcessor(JetStreamConnectorIncomingConfiguration configuration) {
        final var connectionConfiguration = ConnectionConfiguration.of(natsConfiguration);
        final var type = ConsumerType.valueOf(configuration.getPublisherType());
        if (ConsumerType.Pull.equals(type)) {
            return new MessagePullPublisherProcessor(connectionFactory,
                    connectionConfiguration,
                    MessagePullPublisherConfiguration.of(configuration));
        } else {
            return new MessagePushPublisherProcessor(connectionFactory,
                    connectionConfiguration,
                    MessagePushPublisherConfiguration.of(configuration));
        }
    }
}
