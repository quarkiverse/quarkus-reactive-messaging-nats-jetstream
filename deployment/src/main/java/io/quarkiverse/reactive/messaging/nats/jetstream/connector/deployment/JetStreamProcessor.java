package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector.CONNECTOR_NAME;
import static io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration.DEFAULT_DATASOURCE;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientBeanCreator;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientBeanDestroyer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ConnectionConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ConnectionConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.TlsContextFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.TracingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.JetStreamResourceInitializer;
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
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.smallrye.common.annotation.Identifier;
import io.vertx.mutiny.core.Vertx;

class JetStreamProcessor {
    static final String FEATURE = "reactive-messaging-nats-jetstream";
    private static final String DATA_SOURCES_CONFIG_PREFIX = "quarkus.messaging.nats.data-sources.";
    private static final String INCOMING_PREFIX = "mp.messaging.incoming.";
    private static final String OUTGOING_PREFIX = "mp.messaging.outgoing.";
    private static final String CONNECTOR_SUFFIX = ".connector";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    void registerReflectiveClasses(BuildProducer<ReflectiveClassBuildItem> producer) {
        producer.produce(ReflectiveClassBuildItem.builder(Options.DEFAULT_DATA_PORT_TYPE).build());
    }

    /**
     * MapStruct's {@code Mappers.getMapper(...)} loads the generated {@code *Impl} class by name via the class
     * loader. That lookup is invisible to the native-image static analyzer (the class name is only ever built at
     * runtime by string concatenation), so without a reflection hint the generated class is not included in the
     * image, the lookup fails, and MapStruct falls back to a {@code ServiceLoader} lookup that then fails too
     * because the mapper interfaces are not public. Register every generated mapper implementation for reflection
     * so the primary, non-ServiceLoader lookup path succeeds.
     */
    @BuildStep
    void registerMapStructMappersForReflection(CombinedIndexBuildItem combinedIndex,
            BuildProducer<ReflectiveClassBuildItem> producer) {
        var mapperAnnotation = DotName.createSimple("org.mapstruct.Mapper");
        for (AnnotationInstance annotation : combinedIndex.getIndex().getAnnotations(mapperAnnotation)) {
            if (annotation.target().kind() == AnnotationTarget.Kind.CLASS) {
                var implementationName = annotation.target().asClass().name().toString() + "Impl";
                producer.produce(ReflectiveClassBuildItem.builder(implementationName).constructors().build());
            }
        }
    }

    @BuildStep
    ExtensionSslNativeSupportBuildItem activateSslNativeSupport() {
        return new ExtensionSslNativeSupportBuildItem(FEATURE);
    }

    @BuildStep
    void initializeSecureRandomRelatedClassesAtRuntime(
            BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem("io.nats.client.support.RandomUtils"));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem("io.nats.client.NUID"));
    }

    @BuildStep
    void createJetStreamConnector(BuildProducer<AdditionalBeanBuildItem> buildProducer) {
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamConnector.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessagePublisherProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageSubscriberProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConnectionConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(TlsContextFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyProducer.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(UuidCorrelationIdHandler.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(PublisherChannelConfigurationFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerChannelConfigurationFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamResourceInitializer.class));
    }

    @BuildStep
    void registerSerializer(BuildProducer<AdditionalBeanBuildItem> buildProducer,
            JetStreamBuildTimeConfiguration configuration) {
        try {
            Class<?> serializerClass = Class.forName(configuration.serializer(), true,
                    JetStreamProcessor.class.getClassLoader());
            if (!Serializer.class.isAssignableFrom(serializerClass)) {
                throw new RuntimeException(String.format(
                        "quarkus.messaging.nats.serializer is set to '%s', but this class does not implement io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer.",
                        configuration.serializer()));
            }
            buildProducer.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(serializerClass)
                    .setDefaultScope(BuiltinScope.APPLICATION.getName())
                    .setUnremovable()
                    .build());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format(
                    "quarkus.messaging.nats.serializer is set to '%s', but this class could not be found on the application classpath.",
                    configuration.serializer()));
        }
    }

    @BuildStep
    void registerTracing(BuildProducer<AdditionalBeanBuildItem> buildProducer, Capabilities capabilities) {
        if (capabilities.isPresent(Capability.OPENTELEMETRY_TRACER)) {
            buildProducer.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(TracingConfiguration.class)
                    .setUnremovable()
                    .build());
        } else {
            buildProducer.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(DisabledTracerFactory.class)
                    .setDefaultScope(BuiltinScope.APPLICATION.getName())
                    .setUnremovable()
                    .build());
        }
    }

    /**
     * Registers one {@code @ApplicationScoped} {@link Client} CDI bean per configured datasource (the default
     * datasource plus every key under {@code quarkus.messaging.nats.data-sources}). Every one of them is qualified
     * with {@code @Identifier(datasource-name)}, so any of them can be looked up dynamically via
     * {@code @Any Instance<Client>} plus
     * {@code io.smallrye.reactive.messaging.providers.helpers.CDIUtils.getInstanceById(...)} (used where the
     * datasource is resolved at runtime, e.g. from a channel's {@code datasource} attribute). The default
     * datasource's {@link Client} additionally carries the CDI {@code @Default} qualifier, so it can also be
     * injected with a plain, unqualified {@code @Inject Client client}. The actual {@link Client} instance is
     * created by {@link ClientBeanCreator} and closed by {@link ClientBeanDestroyer} on
     * application shutdown.
     */
    @BuildStep
    void createClients(BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        for (String datasource : datasourceNames()) {
            final var configurator = SyntheticBeanBuildItem.configure(Client.class)
                    .types(Client.class)
                    .scope(ApplicationScoped.class)
                    .addQualifier().annotation(Identifier.class).addValue("value", datasource).done()
                    .unremovable()
                    .addInjectionPoint(ClassType.create(ConnectorConfiguration.class))
                    .addInjectionPoint(ClassType.create(ConnectionConfigurationMapper.class))
                    .addInjectionPoint(ClassType.create(Serializer.class))
                    .addInjectionPoint(ClassType.create(TracerFactory.class))
                    .addInjectionPoint(ClassType.create(Vertx.class))
                    .addInjectionPoint(ClassType.create(ExecutorService.class))
                    .param(ClientBeanCreator.DATASOURCE_PARAM, datasource)
                    .creator(ClientBeanCreator.class)
                    .destroyer(ClientBeanDestroyer.class);
            if (DEFAULT_DATASOURCE.equals(datasource)) {
                // Also expose the default datasource's Client as the CDI @Default bean, so it can be injected
                // with a plain @Inject Client client (unqualified), not just @Inject @Identifier("default") Client
                // client. Named datasources only get the @Identifier qualifier, so @Default stays unambiguous.
                configurator.addQualifier(Default.class);
            }
            syntheticBeans.produce(configurator.done());
        }
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

    /**
     * Discovers the configured datasource names from the raw configuration property names, since
     * {@code quarkus.messaging.nats.data-sources} is a {@code RUN_TIME}-phase config map and its keys are
     * therefore not otherwise visible at build time. Always includes
     * {@link io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConnectorConfiguration#DEFAULT_DATASOURCE}.
     */
    private Set<String> datasourceNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add(DEFAULT_DATASOURCE);
        for (String propertyName : ConfigProvider.getConfig().getPropertyNames()) {
            if (propertyName.startsWith(DATA_SOURCES_CONFIG_PREFIX)) {
                final var remainder = propertyName.substring(DATA_SOURCES_CONFIG_PREFIX.length());
                final var name = firstSegment(remainder);
                if (!name.isEmpty()) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    /**
     * Returns the first {@code .}-separated segment of a (possibly quoted, SmallRye-Config style) map key path,
     * e.g. {@code connection.servers[0]} -> {@code connection}, or {@code "my.datasource".connection} ->
     * {@code my.datasource}.
     */
    private String firstSegment(String remainder) {
        if (remainder.startsWith("\"")) {
            final int end = remainder.indexOf('"', 1);
            if (end > 0) {
                return remainder.substring(1, end);
            }
        }
        final int dot = remainder.indexOf('.');
        return dot > 0 ? remainder.substring(0, dot) : remainder;
    }
}
