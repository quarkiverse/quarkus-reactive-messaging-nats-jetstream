package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.DisabledTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.OpenTelemetryTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.ConnectionConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.TlsContextFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.client.VertxClientRegistry;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.ChannelConfigurationFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration.JetStreamRecorder;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.message.JacksonSerializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors.subscriber.MessageSubscriberProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.RequestReplyProducer;
import io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply.UuidCorrelationIdHandler;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
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

class JetStreamProcessor {
    static final String FEATURE = "reactive-messaging-nats-jetstream";

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
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(VertxClientRegistry.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JacksonSerializer.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessagePublisherProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageSubscriberProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConnectionConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(TlsContextFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(RequestReplyProducer.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(UuidCorrelationIdHandler.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ChannelConfigurationFactoryImpl.class));
    }

    @BuildStep
    void registerSerializer(BuildProducer<AdditionalBeanBuildItem> buildProducer,
            JetStreamBuildTimeConfiguration configuration) {
        buildProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(configuration.serializer())
                .setDefaultScope(BuiltinScope.APPLICATION.getName())
                .setUnremovable()
                .build());
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

    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    public void configureJetStream(JetStreamRecorder recorder) {
        recorder.setup();
    }
}
