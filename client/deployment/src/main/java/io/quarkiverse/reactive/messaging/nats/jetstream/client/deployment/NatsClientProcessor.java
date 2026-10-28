package io.quarkiverse.reactive.messaging.nats.jetstream.client.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ClientConfiguration.DEFAULT_DATASOURCE;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientBeanCreator;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientBeanDestroyer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamResourceInitializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ClientConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.TlsContextFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.TracingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.ExcludedTypeBuildItem;
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
import io.quarkus.deployment.builditem.nativeimage.ReflectiveMethodBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.smallrye.common.annotation.Identifier;
import io.vertx.mutiny.core.Vertx;

class NatsClientProcessor {
    static final String FEATURE = "nats-client";
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
     * The client thread pool looks up the virtual thread API by reflection, so the extension still runs on Java 17.
     * Register those methods, so virtual threads also work in a native image built with Java 21 or later.
     */
    @BuildStep
    void registerVirtualThreadMethodsForReflection(BuildProducer<ReflectiveMethodBuildItem> producer) {
        producer.produce(new ReflectiveMethodBuildItem(Thread.class.getName(), "ofVirtual", new Class<?>[0]));
        producer.produce(new ReflectiveMethodBuildItem("java.lang.Thread$Builder", "name", String.class, long.class));
        producer.produce(new ReflectiveMethodBuildItem("java.lang.Thread$Builder", "factory", new Class<?>[0]));
        producer.produce(new ReflectiveMethodBuildItem(Executors.class.getName(), "newThreadPerTaskExecutor",
                ThreadFactory.class));
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
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> buildProducer) {
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConnectionConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(TlsContextFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamResourceInitializer.class));
    }

    @BuildStep
    void registerSerializer(BuildProducer<AdditionalBeanBuildItem> buildProducer,
            ClientBuildTimeConfiguration configuration) {
        try {
            Class<?> serializerClass = Class.forName(configuration.serializer(), true,
                    NatsClientProcessor.class.getClassLoader());
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

    /**
     * Registers the OpenTelemetry {@link TracerFactory} producer when the OpenTelemetry tracer is present, and the
     * {@link DisabledTracerFactory} otherwise. The runtime jar is indexed, so {@link TracingConfiguration} would also
     * be discovered as a bean on its own; it is excluded when OpenTelemetry is absent.
     */
    @BuildStep
    void registerTracing(BuildProducer<AdditionalBeanBuildItem> buildProducer,
            BuildProducer<ExcludedTypeBuildItem> excludedTypes, Capabilities capabilities) {
        if (capabilities.isPresent(Capability.OPENTELEMETRY_TRACER)) {
            buildProducer.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(TracingConfiguration.class)
                    .setUnremovable()
                    .build());
        } else {
            excludedTypes.produce(new ExcludedTypeBuildItem(TracingConfiguration.class.getName()));
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
     * {@code @Any Instance<Client>} selected with that qualifier (used where the datasource is resolved at runtime,
     * e.g. from a channel's {@code datasource} attribute). The default
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
                    .addInjectionPoint(ClassType.create(ClientConfiguration.class))
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
     * Discovers the configured datasource names from the raw configuration property names, since
     * {@code quarkus.messaging.nats.data-sources} is a {@code RUN_TIME}-phase config map and its keys are
     * therefore not otherwise visible at build time. Always includes
     * {@link ClientConfiguration#DEFAULT_DATASOURCE}.
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
