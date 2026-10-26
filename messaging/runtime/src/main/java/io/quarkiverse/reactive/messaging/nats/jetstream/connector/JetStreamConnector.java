package io.quarkiverse.reactive.messaging.nats.jetstream.connector;

import static io.smallrye.reactive.messaging.annotations.ConnectorAttribute.Direction.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Flow;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessagePublisherProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageSubscriberProcessor;
import io.smallrye.reactive.messaging.annotations.ConnectorAttribute;
import io.smallrye.reactive.messaging.connector.InboundConnector;
import io.smallrye.reactive.messaging.connector.OutboundConnector;
import io.smallrye.reactive.messaging.health.HealthReport;
import io.smallrye.reactive.messaging.health.HealthReporter;
import io.smallrye.reactive.messaging.providers.helpers.CDIUtils;

@SuppressWarnings({ "rawtypes" })
@ApplicationScoped
@Connector(JetStreamConnector.CONNECTOR_NAME)
@ConnectorAttribute(name = "stream", description = "The name of the stream", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "subject", description = "The name of the subject", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "consumer", description = "The name of the consumer", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "payload-type", description = "The payload type", direction = INCOMING, type = "String")
@ConnectorAttribute(name = "batch-size", description = "The batch size", direction = INCOMING, type = "int", defaultValue = "100")
@ConnectorAttribute(name = "timeout", description = "The timeout in milliseconds for pulling messages", direction = INCOMING, type = "Long", defaultValue = "1000")
@ConnectorAttribute(name = "retry-backoff", description = "The retry backoff in milliseconds for retry processing messages", direction = INCOMING_AND_OUTGOING, type = "Long")
@ConnectorAttribute(name = "datasource", description = "The name of the datasource", direction = INCOMING_AND_OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.subject", description = "The subject on which replies are expected when using JetStreamRequestReply. Defaults to the channel subject with '.replies' appended.", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.timeout", description = "How long to wait for a reply in milliseconds when using JetStreamRequestReply", direction = OUTGOING, type = "Long", defaultValue = "5000")
@ConnectorAttribute(name = "reply.inactive-threshold", description = "How long NATS keeps an idle request/reply consumer before reclaiming it, in milliseconds", direction = OUTGOING, type = "Long", defaultValue = "60000")
@ConnectorAttribute(name = "reply.correlation-id.handler", description = "The @Identifier of the CorrelationIdHandler bean used to generate and parse correlation ids (default 'uuid')", direction = OUTGOING, type = "String")
@ConnectorAttribute(name = "reply.failure.handler", description = "The @Identifier of the ReplyFailureHandler bean used to turn reply payloads into failures", direction = OUTGOING, type = "String")
public class JetStreamConnector implements InboundConnector, OutboundConnector, HealthReporter {
    public static final String CONNECTOR_NAME = "quarkus-jetstream";

    private final Instance<MessagePublisherProcessor> publisherProcessors;
    private final Instance<MessageSubscriberProcessor> subscriberProcessors;

    public JetStreamConnector(@Any Instance<MessagePublisherProcessor> publisherProcessors,
            @Any Instance<MessageSubscriberProcessor> subscriberProcessors) {
        this.publisherProcessors = publisherProcessors;
        this.subscriberProcessors = subscriberProcessors;
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Override
    public Flow.Publisher<? extends Message<?>> getPublisher(Config config) {
        final var channel = config.getValue(ConnectorFactory.CHANNEL_NAME_ATTRIBUTE, String.class);
        final var instance = CDIUtils.getInstanceById(publisherProcessors, channel);
        final MessagePublisherProcessor<?> processor = instance.get();
        return processor.publisher();
    }

    @Override
    public Flow.Subscriber<? extends Message<?>> getSubscriber(Config config) {
        final var channel = config.getValue(ConnectorFactory.CHANNEL_NAME_ATTRIBUTE, String.class);
        final var instance = CDIUtils.getInstanceById(subscriberProcessors, channel);
        final MessageSubscriberProcessor<?> processor = instance.get();
        return processor.subscriber();
    }

    @Override
    public HealthReport getReadiness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        messageProcessors().forEach(processor -> builder.add(new HealthReport.ChannelInfo(
                processor.channelConfiguration().name(),
                processor.health().healthy(),
                processor.health().message())));
        return builder.build();
    }

    @Override
    public HealthReport getLiveness() {
        final HealthReport.HealthReportBuilder builder = HealthReport.builder();
        messageProcessors().forEach(processor -> builder.add(new HealthReport.ChannelInfo(
                processor.channelConfiguration().name(),
                processor.health().healthy(),
                processor.health().message())));
        return builder.build();
    }

    private Collection<MessageProcessor> messageProcessors() {
        final var processors = new ArrayList<MessageProcessor>();
        publisherProcessors.forEach(processors::add);
        subscriberProcessors.forEach(processors::add);
        return processors;
    }
}
