package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector.CONNECTOR_NAME;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.ClassType;

import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageProcessorBeanDestroyer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessagePublisherProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessagePublisherProcessorBeanCreator;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageSubscriberProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageSubscriberProcessorBeanCreator;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.MessageSubscriberProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyProducer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.UuidCorrelationIdHandler;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.smallrye.common.annotation.Identifier;

class JetStreamProcessor {
    static final String FEATURE = "reactive-messaging-nats-jetstream";
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";
    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";
    private static final String CONNECTOR_SUFFIX = ".connector";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void createJetStreamConnector(BuildProducer<AdditionalBeanBuildItem> buildProducer) {
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamConnector.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessagePublisherProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageSubscriberProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyProducer.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(UuidCorrelationIdHandler.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(PublisherChannelConfigurationFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerChannelConfigurationFactoryImpl.class));
    }

    /**
     * Registers one {@code @Singleton} {@link MessagePublisherProcessor} CDI bean per incoming channel and one
     * {@link MessageSubscriberProcessor} per outgoing channel that is configured with this connector. Each is
     * qualified with {@code @Identifier(channel-name)} and looked up by the connector when SmallRye asks for the
     * channel's publisher or subscriber. Being CDI managed, a processor is stopped by
     * {@link MessageProcessorBeanDestroyer} on application shutdown. {@code @Singleton} is used instead of
     * {@code @ApplicationScoped} so no client proxy is needed for the generic processor classes.
     */
    @BuildStep
    void createProcessors(BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        for (String channel : channelNames(INCOMING_PREFIX)) {
            syntheticBeans.produce(SyntheticBeanBuildItem.configure(MessagePublisherProcessor.class)
                    .types(MessagePublisherProcessor.class)
                    .scope(Singleton.class)
                    .addQualifier().annotation(Identifier.class).addValue("value", channel).done()
                    .unremovable()
                    .addInjectionPoint(ClassType.create(Config.class))
                    .addInjectionPoint(ClassType.create(MessagePublisherProcessorFactory.class))
                    .param(MessagePublisherProcessorBeanCreator.CHANNEL_PARAM, channel)
                    .creator(MessagePublisherProcessorBeanCreator.class)
                    .destroyer(MessageProcessorBeanDestroyer.class)
                    .done());
        }
        for (String channel : channelNames(OUTGOING_PREFIX)) {
            syntheticBeans.produce(SyntheticBeanBuildItem.configure(MessageSubscriberProcessor.class)
                    .types(MessageSubscriberProcessor.class)
                    .scope(Singleton.class)
                    .addQualifier().annotation(Identifier.class).addValue("value", channel).done()
                    .unremovable()
                    .addInjectionPoint(ClassType.create(Config.class))
                    .addInjectionPoint(ClassType.create(MessageSubscriberProcessorFactory.class))
                    .param(MessageSubscriberProcessorBeanCreator.CHANNEL_PARAM, channel)
                    .creator(MessageSubscriberProcessorBeanCreator.class)
                    .destroyer(MessageProcessorBeanDestroyer.class)
                    .done());
        }
    }

    /**
     * Discovers the channels under the given prefix ({@code mp.messaging.incoming.} or {@code mp.messaging.outgoing.})
     * whose {@code connector} attribute is this connector. Channels that are only defined at runtime (for example
     * through environment variables alone) are not visible here; the connector falls back to an unmanaged processor
     * for those.
     */
    private Set<String> channelNames(String prefix) {
        final var config = ConfigProvider.getConfig();
        final Set<String> names = new LinkedHashSet<>();
        for (String propertyName : config.getPropertyNames()) {
            if (propertyName.startsWith(prefix) && propertyName.endsWith(CONNECTOR_SUFFIX)
                    && CONNECTOR_NAME.equals(config.getOptionalValue(propertyName, String.class).orElse(null))) {
                var name = propertyName.substring(prefix.length(), propertyName.length() - CONNECTOR_SUFFIX.length());
                if (name.length() > 1 && name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }
}
