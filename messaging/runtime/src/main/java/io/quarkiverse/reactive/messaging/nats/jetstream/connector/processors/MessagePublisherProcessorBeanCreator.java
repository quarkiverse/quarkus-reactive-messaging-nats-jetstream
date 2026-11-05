package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import static org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory.INCOMING_PREFIX;

import org.eclipse.microprofile.config.Config;

import io.quarkus.arc.BeanCreator;
import io.quarkus.arc.SyntheticCreationalContext;
import io.smallrye.reactive.messaging.providers.impl.ConcurrencyConnectorConfig;
import io.smallrye.reactive.messaging.providers.impl.ConnectorConfig;

/**
 * Creates the {@link MessagePublisherProcessor} synthetic CDI bean of an incoming channel. One bean is registered per
 * configured channel by {@code JetStreamProcessor}, which passes the channel name as the {@value #CHANNEL_PARAM}
 * parameter and declares every bean resolved here as an injection point. Channels configured with the
 * {@code concurrency} attribute get one bean per concurrent copy, identified by the {@value #INDEX_PARAM} parameter,
 * matching the {@code channel$index} channel names SmallRye Reactive Messaging asks the connector for.
 */
@SuppressWarnings("rawtypes")
public class MessagePublisherProcessorBeanCreator implements BeanCreator<MessagePublisherProcessor> {
    public static final String CHANNEL_PARAM = "channel";
    public static final String INDEX_PARAM = "index";

    @Override
    public MessagePublisherProcessor create(SyntheticCreationalContext<MessagePublisherProcessor> context) {
        final var channel = (String) context.getParams().get(CHANNEL_PARAM);
        final var index = (Integer) context.getParams().get(INDEX_PARAM);
        final var rootConfig = context.getInjectedReference(Config.class);
        final Config config = index == null
                ? ConnectorConfig.create(INCOMING_PREFIX, rootConfig, channel)
                : new ConcurrencyConnectorConfig(INCOMING_PREFIX, rootConfig,
                        ConnectorConfig.getConnectorAttribute(INCOMING_PREFIX, rootConfig, channel), channel, index);
        return context.getInjectedReference(MessagePublisherProcessorFactory.class).create(config);
    }
}
