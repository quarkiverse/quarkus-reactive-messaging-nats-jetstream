package io.quarkiverse.reactive.messaging.nats.jetstream.connector.deployment;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
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
     * Retrieves the serializer implementation used for message serialization and deserialization.
     * The default implementation is
     * {@code io.quarkiverse.reactive.messaging.nats.jetstream.connector.message.JacksonSerializer}.
     *
     * @return the {@link Serializer} instance used for handling serialization of messages.
     */
    @WithDefault("io.quarkiverse.reactive.messaging.nats.jetstream.connector.message.JacksonSerializer")
    Class<? extends Serializer> serializer();

    /**
     * Dev Services configuration.
     */
    JetStreamDevServicesBuildTimeConfiguration devservices();
}
