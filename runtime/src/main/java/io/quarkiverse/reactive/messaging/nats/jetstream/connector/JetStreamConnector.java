package io.quarkiverse.reactive.messaging.nats.jetstream.connector;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber.MessageSubscriberProcessorFactory;
import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.connector.OutboundConnector;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.health.HealthReporter;

@ApplicationScoped
@Connector(JetStreamConnector.CONNECTOR_NAME)
@ConnectorAttribute(name = "stream", description = "The name of the stream", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "subject", description = "The name of the subject", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "consumer", description = "The name of the consumer", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "payload-type", description = "The payload type", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "batch-size", description = "The batch size", direction = INCOMING, type = "Integer", defaultValue = "100")
@ConnectorAttribute(name = "timeout", description = "The timeout in milliseconds for pulling messages", direction = INCOMING, type = "Long", defaultValue = "1000")
@ConnectorAttribute(name = "retry-backoff", description = "The retry backoff in milliseconds for retry processing messages", direction = INCOMING_AND_OUTGOING, type = "Long", defaultValue = "10000")
@ConnectorAttribute(name = "datasource", description = "The name of the datasource", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.subject", description = "The subject on which replies are expected when using JetStreamRequestReply. Defaults to the channel subject with '.replies' appended.", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.timeout", description = "How long to wait for a reply in milliseconds when using JetStreamRequestReply", direction = OUTGOING, type = "Long", defaultValue = "5000")
@ConnectorAttribute(name = "reply.inactive-threshold", description = "How long NATS keeps an idle request/reply consumer before reclaiming it, in milliseconds", direction = OUTGOING, type = "Long", defaultValue = "60000")
@ConnectorAttribute(name = "reply.correlation-id.handler", description = "The @Identifier of the CorrelationIdHandler bean used to generate and parse correlation ids (default 'uuid')", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.failure.handler", description = "The @Identifier of the ReplyFailureHandler bean used to turn reply payloads into failures", direction = OUTGOING, type = "String")
public class JetStreamConnector implements InboundConnector, OutboundConnector, HealthReporter {
    public static final String CONNECTOR_NAME = "quarkus-jetstream";

    private final List<MessageProcessor> processors;
    private final MessageSubscriberProcessorFactory messageSubscriberProcessorFactory;
    private final MessagePublisherProcessorFactory messagePublisherProcessorFactory;

    public JetStreamConnector(MessageSubscriberProcessorFactory messageSubscriberProcessorFactory,
            MessagePublisherProcessorFactory messagePublisherProcessorFactory) {
        this.processors = new CopyOnWriteArrayList<>();
        this.messageSubscriberProcessorFactory = messageSubscriberProcessorFactory;
        this.messagePublisherProcessorFactory = messagePublisherProcessorFactory;
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Flow.Publisher<? extends Message<?>> getPublisher(Config config) {
        final var configuration = new JetStreamConnectorIncomingConfiguration(config);
        final var processor = messagePublisherProcessorFactory.create(configuration);
        processors.add(processor);
        return processor.publisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var configuration = new JetStreamConnectorOutgoingConfiguration(config);
        final var processor = messageSubscriberProcessorFactory.create(configuration);
        processors.add(processor);
        return processor.subscriber();
    }

    @Override
    public HealthReport getReadiness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        processors.forEach(processor -> builder.add(new HealthReport.ChannelInfo(
                processor.channel(),
                processor.health().healthy(),
                processor.health().message())));
        return builder.build();
    }

    @Override
    public HealthReport getLiveness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        processors.forEach(processor -> builder.add(new HealthReport.ChannelInfo(
                processor.channel(),
                processor.health().healthy(),
                processor.health().message())));
        return builder.build();
    }

    public void onShutdown(@Observes ShutdownEvent event) {
        processors.forEach(MessageProcessor::stop);
    }
}
