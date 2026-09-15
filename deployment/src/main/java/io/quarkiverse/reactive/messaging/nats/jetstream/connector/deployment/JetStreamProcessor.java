package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ClientBeanDestroyer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ConnectionConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.TlsContextFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ConsumerChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.JetStreamRecorder;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.PublisherChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber.MessageSubscriberProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyProducer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.UuidCorrelationIdHandler;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.Capability;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.smallrye.common.annotation.Identifier;

class JetStreamProcessor {
    static final String FEATURE = "reactive-messaging-nats-jetstream";
    private static final String DATA_SOURCES_CONFIG_PREFIX = "quarkus.messaging.nats.data-sources.";

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
                    .addBeanClass(OpenTelemetryTracerFactory.class)
                    .setDefaultScope(BuiltinScope.APPLICATION.getName())
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
     * The {@link Client} synthetic bean created below resolves the default {@link ExecutorService} bean with a
     * plain {@code CDI.current().select(ExecutorService.class)} lookup inside {@link JetStreamRecorder#createClient},
     * rather than through a declared injection point. ArC's unused-bean removal can't see that lookup, so without
     * this it removes the (otherwise unreferenced) default {@code ExecutorService} bean and that lookup fails at
     * runtime with an unsatisfied-dependency error.
     */
    @BuildStep
    void keepExecutorServiceUnremovable(BuildProducer<UnremovableBeanBuildItem> producer) {
        producer.produce(UnremovableBeanBuildItem.beanTypes(DotName.createSimple(ExecutorService.class.getName())));
    }

    /**
     * Registers one {@code @ApplicationScoped} {@link Client} CDI bean per configured datasource (the default
     * datasource plus every key under {@code quarkus.messaging.nats.data-sources}), each qualified with
     * {@code @Identifier(datasource-name)} so it can be injected directly (for the default/a known datasource) or
     * looked up dynamically via {@code @Any Instance<Client>} plus
     * {@code io.smallrye.reactive.messaging.providers.helpers.CDIUtils.getInstanceById(...)} (for a datasource
     * resolved at runtime, e.g. from a channel's {@code datasource} attribute). The actual {@link Client} instance
     * is created by {@link JetStreamRecorder#createClient(String)} and closed by {@link ClientBeanDestroyer} on
     * application shutdown.
     */
    @BuildStep
    @Record(RUNTIME_INIT)
    void createClients(JetStreamRecorder recorder, BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        for (String datasource : datasourceNames()) {
            syntheticBeans.produce(SyntheticBeanBuildItem.configure(Client.class)
                    .types(Client.class)
                    .scope(ApplicationScoped.class)
                    .addQualifier().annotation(Identifier.class).addValue("value", datasource).done()
                    .unremovable()
                    .setRuntimeInit()
                    .createWith(recorder.createClient(datasource))
                    .destroyer(ClientBeanDestroyer.class)
                    .done());
        }
    }

    /**
     * Discovers the configured datasource names from the raw configuration property names, since
     * {@code quarkus.messaging.nats.data-sources} is a {@code RUN_TIME}-phase config map and its keys are
     * therefore not otherwise visible at build time. Always includes
     * {@link JetStreamConnector#DEFAULT_DATASOURCE}.
     */
    private Set<String> datasourceNames() {
        final Set<String> names = new LinkedHashSet<>();
        names.add(JetStreamConnector.DEFAULT_DATASOURCE);
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

    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    public void configureJetStream(JetStreamRecorder recorder) {
        recorder.setup();
    }
}
