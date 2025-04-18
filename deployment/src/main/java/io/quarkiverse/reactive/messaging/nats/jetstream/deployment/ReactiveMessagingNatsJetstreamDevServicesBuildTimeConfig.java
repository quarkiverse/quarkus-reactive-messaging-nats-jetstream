package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats.jet-stream.devservices")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ReactiveMessagingNatsJetstreamDevServicesBuildTimeConfig {

    /**
     * If Dev Services for NATS JetStream has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present.
     */
    Optional<Boolean> enabled();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    Optional<Integer> port();

    /**
     * The image to use.
     * Note that only NATS.io images are supported.
     *
     * Check https://hub.docker.com/_/nats to find the available versions.
     */
    @WithDefault("nats:2.11")
    String imageName();

    /**
     * Indicates if the NATS JetStream broker managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for NATS JetStream starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-jetstream} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @WithDefault("true")
    boolean shared();

    /**
     * The value of the {@code quarkus-dev-service-jetstream} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for NATS JetStream looks for a container with the
     * {@code quarkus-dev-service-jetstream} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code quarkus-dev-service-jetstream} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared NATS JetStream brokers.
     */
    @WithDefault("nats")
    String serviceName();
}
