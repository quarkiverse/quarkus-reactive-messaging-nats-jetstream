package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import java.util.Optional;
import java.util.OptionalInt;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.messaging.nats.devservices")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface JetStreamDevServicesBuildTimeConfiguration {

    /**
     * If Dev Services for NATS JetStream has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present.
     */
    @WithDefault(value = "true")
    Boolean enabled();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    OptionalInt port();

    /**
     * The image to use.
     * Note that only NATS.io images are supported.
     * Check <a href="https://hub.docker.com/_/nats">NATS</a> to find the available versions.
     */
    @WithDefault("nats:2.12")
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

    /**
     * Retrieves the username to be used for authenticating with the NATS JetStream broker.
     * If not explicitly configured, the default value is "guest".
     *
     * @return the username for authentication
     */
    @WithDefault("guest")
    String username();

    /**
     * Returns the password to be used for authenticating with the NATS JetStream broker.
     * If not explicitly configured, the default value is "guest".
     *
     * @return the password for authentication
     */
    @WithDefault("guest")
    String password();

    /**
     * TLS configuration
     */
    Optional<TlsConfiguration> tlsConfiguration();

    interface TlsConfiguration {

        /**
         * The absolute path to the PEM certificate file.
         */
        String certificateFile();

        /**
         * The absolut path to the PEM key file.
         */
        String keyFile();
    }
}
