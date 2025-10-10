package io.quarkiverse.reactive.messaging.nats.jetstream;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConfigurationException;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberProcessorFactory;
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
@ConnectorAttribute(name = "retry-backoff", description = "The retry backoff in milliseconds for retry processing messages", direction = INCOMING_AND_OUTGOING, type = "Long", defaultValue = "10000")
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
        final var channel = configuration.getChannel();
        final var stream = configuration.getStream().orElseThrow(
                () -> new ConfigurationException("The 'stream' attribute must be set for the JetStream connector."));
        final var consumer = configuration.getConsumer().orElseThrow(
                () -> new ConfigurationException("The 'consumer' attribute must be set for the JetStream connector."));
        final var retryBackoff = Duration.ofMillis(configuration.getRetryBackoff());
        final var processor = messagePublisherProcessorFactory.create(channel, stream, consumer, retryBackoff);
        processors.add(processor);
        return processor.publisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var configuration = new JetStreamConnectorOutgoingConfiguration(config);
        final var channel = configuration.getChannel();
        final var stream = configuration.getStream().orElseThrow(
                () -> new ConfigurationException("The 'stream' attribute must be set for the JetStream connector."));
        final var subject = configuration.getSubject().orElseThrow(
                () -> new ConfigurationException("The 'subject' attribute must be set for the JetStream connector."));
        final var retryBackoff = Duration.ofMillis(configuration.getRetryBackoff());
        final var processor = messageSubscriberProcessorFactory.create(channel, stream, subject, retryBackoff);
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

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
    }
}
