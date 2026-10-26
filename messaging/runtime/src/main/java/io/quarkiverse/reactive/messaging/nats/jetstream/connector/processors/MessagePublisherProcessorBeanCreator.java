package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import static org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory.INCOMING_PREFIX;

import org.eclipse.microprofile.config.Config;

import io.quarkus.arc.BeanCreator;
import io.quarkus.arc.SyntheticCreationalContext;
import io.smallrye.reactive.messaging.providers.impl.ConnectorConfig;

/**
 * Creates the {@link MessagePublisherProcessor} synthetic CDI bean of an incoming channel. One bean is registered per
 * configured channel by {@code JetStreamProcessor}, which passes the channel name as the {@value #CHANNEL_PARAM}
 * parameter and declares every bean resolved here as an injection point.
 */
@SuppressWarnings("rawtypes")
public class MessagePublisherProcessorBeanCreator implements BeanCreator<MessagePublisherProcessor> {
    public static final String CHANNEL_PARAM = "channel";

    @Override
    public MessagePublisherProcessor create(SyntheticCreationalContext<MessagePublisherProcessor> context) {
        final var channel = (String) context.getParams().get(CHANNEL_PARAM);
        final var config = ConnectorConfig.create(INCOMING_PREFIX, context.getInjectedReference(Config.class), channel);
        return context.getInjectedReference(MessagePublisherProcessorFactory.class).create(config);
    }
}
