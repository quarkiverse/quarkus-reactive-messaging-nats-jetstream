package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerAwareImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreAwareImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamAwareImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamStateMapperImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactoryImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamRecorder;
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
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ClientImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConnectionFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerAwareImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ConsumerMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(HeaderMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(PayloadMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(SerializerImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(KeyValueStoreAwareImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(KeyValueConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(StreamAwareImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(StreamConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(StreamStateMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(TracerFactoryImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessagePublisherProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageSubscriberProcessorFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(
                io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.ConsumerConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(
                io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.KeyValueStoreConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(
                io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PullConsumerConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(
                io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.PushConsumerConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(
                io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper.StreamConfigurationMapperImpl.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ExecutionHolder.class));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    @Consume(SyntheticBeansRuntimeInitBuildItem.class)
    public void configureJetStream(JetStreamRecorder recorder) {
        recorder.setup();
    }
}
