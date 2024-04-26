package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnector;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamRecorder;
import io.quarkiverse.reactive.messaging.nats.jetstream.administration.MessageResolver;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPublisher;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.MessageFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.tracing.JetStreamInstrumenter;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.JetStreamUtility;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

class ReactiveMesssagingNatsJetstreamProcessor {

    static final String FEATURE = "reactive-messsaging-nats-jetstream";

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
    void createNatsConnector(BuildProducer<AdditionalBeanBuildItem> buildProducer) {
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamConnector.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageResolver.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(PayloadMapper.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamInstrumenter.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(ExecutionHolder.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamPublisher.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(MessageFactory.class));
        buildProducer.produce(AdditionalBeanBuildItem.unremovableOf(JetStreamUtility.class));
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public void configureJetStream(JetStreamRecorder recorder,
            JetStreamBuildConfiguration buildConfig) {
        if (buildConfig.autoConfigure()) {
            recorder.setupStreams();
        }
    }
}
