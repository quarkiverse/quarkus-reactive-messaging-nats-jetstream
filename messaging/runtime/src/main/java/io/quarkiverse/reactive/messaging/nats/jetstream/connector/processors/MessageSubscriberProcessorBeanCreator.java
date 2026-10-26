package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import static org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory.OUTGOING_PREFIX;

import org.eclipse.microprofile.config.Config;

import io.quarkus.arc.BeanCreator;
import io.quarkus.arc.SyntheticCreationalContext;
import io.smallrye.reactive.messaging.providers.impl.ConnectorConfig;

/**
 * Creates the {@link MessageSubscriberProcessor} synthetic CDI bean of an outgoing channel. One bean is registered per
 * configured channel by {@code JetStreamProcessor}, which passes the channel name as the {@value #CHANNEL_PARAM}
 * parameter and declares every bean resolved here as an injection point.
 */
@SuppressWarnings("rawtypes")
public class MessageSubscriberProcessorBeanCreator implements BeanCreator<MessageSubscriberProcessor> {
    public static final String CHANNEL_PARAM = "channel";

    @Override
    public MessageSubscriberProcessor create(SyntheticCreationalContext<MessageSubscriberProcessor> context) {
        final var channel = (String) context.getParams().get(CHANNEL_PARAM);
        final var config = ConnectorConfig.create(OUTGOING_PREFIX, context.getInjectedReference(Config.class), channel);
        return context.getInjectedReference(MessageSubscriberProcessorFactory.class).create(config);
    }
}
