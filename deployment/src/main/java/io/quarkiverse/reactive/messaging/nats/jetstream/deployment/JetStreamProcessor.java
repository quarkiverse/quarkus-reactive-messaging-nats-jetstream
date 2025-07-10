package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamRecorder;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.DefaultConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.DefaultTracerFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.ConsumerMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultMessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.DefaultPayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.StreamStateMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePublisherProcessorFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber.MessageSubscriberProcessorFactory;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.SyntheticBeansRuntimeInitBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Consume;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

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
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(DefaultTracerFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ExecutionHolder.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(DefaultConnectionFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(DefaultPayloadMapper.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(DefaultMessageMapper.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(StreamStateMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(DefaultTracerFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessagePublisherProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageSubscriberProcessorFactory.class));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    public void configureJetStream(JetStreamRecorder recorder) {
        recorder.setup();
    }
}
