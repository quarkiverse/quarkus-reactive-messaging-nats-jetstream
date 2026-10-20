package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Build-time configuration interface for JetStream messaging in the Quarkus framework.
 * This interface defines configuration properties for the NATS JetStream messaging extension,
 * which are applied during the build phase of the application. The settings specified here
 * determine aspects of how messages are serialized and deserialized in the application.
 * Configuration properties are prefixed with {@code quarkus.messaging.nats}.
 */
@ConfigMapping(prefix = "quarkus.messaging.nats")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface JetStreamBuildTimeConfiguration {

    /**
     * The fully-qualified class name of the serializer implementation used for message serialization
     * and deserialization. The class must implement
     * {@code io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer}.
     * The default is
     * {@code io.quarkiverse.reactive.messaging.nats.jetstream.client.message.JacksonSerializer}.
     *
     * @return the fully-qualified class name of the serializer implementation.
     */
    @WithDefault("io.quarkiverse.reactive.messaging.nats.jetstream.client.message.JacksonSerializer")
    String serializer();

    /**
     * Dev Services configuration.
     */
    JetStreamDevServicesBuildTimeConfiguration devservices();
}
