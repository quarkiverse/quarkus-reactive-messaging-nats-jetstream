package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberProcessorFactory;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.connector.OutboundConnector;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.health.HealthReporter;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BeforeDestroyed;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Reception;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Flow;

@ApplicationScoped
@Connector(JetStreamConnector.CONNECTOR_NAME)
public class JetStreamConnector implements InboundConnector, OutboundConnector, HealthReporter {
    public static final String CONNECTOR_NAME = "quarkus-jetstream";

    private final static Logger logger = Logger.getLogger(JetStreamConnector.class);

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
        final var channel = config.getValue("channel", String.class);
        final var processor = messagePublisherProcessorFactory.create(channel);
        processors.add(processor);
        return processor.publisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var channel = config.getValue("channel", String.class);
        final var processor = messageSubscriberProcessorFactory.create(channel);
        processors.add(processor);
        return processor.subscriber();
    }

    @Override
    public HealthReport getReadiness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        processors.forEach(client -> builder.add(new HealthReport.ChannelInfo(
                client.channel(),
                client.readiness().healthy(),
                client.readiness().message())));
        return builder.build();
    }

    @Override
    public HealthReport getLiveness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        processors.forEach(client -> builder.add(new HealthReport.ChannelInfo(
                client.channel(),
                client.liveness().healthy(),
                client.liveness().message())));
        return builder.build();
    }

    public void terminate(
            @Observes(notifyObserver = Reception.IF_EXISTS) @Priority(50) @BeforeDestroyed(ApplicationScoped.class) Object ignored) {
        this.processors.forEach(processor -> {
            try {
                processor.close();
            } catch (Exception failure) {
                logger.warnf(failure, "Failed to close the processor: %s", failure.getMessage());
            }
        });
        this.processors.clear();
    }
}
