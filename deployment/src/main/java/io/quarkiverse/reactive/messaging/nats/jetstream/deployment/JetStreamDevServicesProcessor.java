package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.deployment.JetStreamContainer.NATS_PORT;
import static io.quarkiverse.reactive.messaging.nats.jetstream.deployment.JetStreamProcessor.FEATURE;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsDevServicesSupportedByLaunchMode;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.*;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ComposeLocator;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.configuration.ConfigUtils;

/**
 * Starts a NATS JetStream broker as dev service if needed.
 * It uses <a href="https://hub.docker.com/nats">NATS</a> as image.
 */
@BuildSteps(onlyIf = { IsDevServicesSupportedByLaunchMode.class, DevServicesConfig.Enabled.class })
public class JetStreamDevServicesProcessor {
    private static final Logger log = Logger.getLogger(JetStreamDevServicesProcessor.class);

    /**
     * Label to add to shared Dev Service for pulsar running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-jetstream";

    private static final ContainerLocator jetStreamContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL, NATS_PORT);

    @SuppressWarnings("resource")
    @BuildStep
    public void startJetstreamContainer(LaunchModeBuildItem launchMode,
            DockerStatusBuildItem dockerStatusBuildItem,
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            JetStreamDevServicesBuildTimeConfig devServicesBuildTimeConfig,
            BuildProducer<DevServicesResultBuildItem> devServicesResult,
            DevServicesConfig devServicesConfig) {

        boolean useSharedNetwork = DevServicesSharedNetworkBuildItem.isSharedNetworkRequired(devServicesConfig,
                devServicesSharedNetworkBuildItem);

        if (!natsDevServicesEnabled(dockerStatusBuildItem, devServicesBuildTimeConfig)) {
            // If the dev services are disabled, we don't need to do anything
            return;
        }

        discoverRunningService(composeProjectBuildItem, launchMode.getLaunchMode(),
                devServicesBuildTimeConfig, useSharedNetwork)
                .ifPresentOrElse(devServicesResult::produce, () -> devServicesResult
                        .produce(DevServicesResultBuildItem.owned().feature(FEATURE)
                                .serviceName(devServicesBuildTimeConfig.serviceName())
                                .startable(
                                        () -> new JetStreamContainer(
                                                DockerImageName.parse(devServicesBuildTimeConfig.imageName()),
                                                devServicesBuildTimeConfig.port(),
                                                composeProjectBuildItem.getDefaultNetworkId(), useSharedNetwork,
                                                devServicesBuildTimeConfig.username(), devServicesBuildTimeConfig.password())
                                                // Dev Service discovery works using a global dev service label applied in DevServicesCustomizerBuildItem
                                                // for backwards compatibility we still add the custom label
                                                .withSharedServiceLabel(launchMode.getLaunchMode(),
                                                        devServicesBuildTimeConfig.serviceName()))
                                .configProvider(Map.of("quarkus.messaging.nats.connection.servers",
                                        s -> "nats://" + s.getConnectionInfo(),
                                        "quarkus.messaging.nats.connection.username",
                                        s -> devServicesBuildTimeConfig.username(),
                                        "quarkus.messaging.nats.connection.password",
                                        s -> devServicesBuildTimeConfig.password(),
                                        "quarkus.messaging.nats.connection.ssl-enabled", s -> "false"))
                                .build()));
    }

    private Optional<DevServicesResultBuildItem> discoverRunningService(
            DevServicesComposeProjectBuildItem composeProjectBuildItem,
            LaunchMode launchMode,
            JetStreamDevServicesBuildTimeConfig devServicesBuildTimeConfig,
            boolean useSharedNetwork) {
        return jetStreamContainerLocator
                .locateContainer(devServicesBuildTimeConfig.serviceName(), devServicesBuildTimeConfig.shared(), launchMode)
                .or(() -> ComposeLocator.locateContainer(composeProjectBuildItem,
                        List.of(devServicesBuildTimeConfig.imageName()),
                        NATS_PORT, launchMode, useSharedNetwork))
                .map(containerAddress -> {
                    String serverUrl = "nats://" + containerAddress.getUrl();
                    return DevServicesResultBuildItem.discovered()
                            .name(FEATURE)
                            .containerId(containerAddress.getId())
                            .config(Map.of("quarkus.messaging.nats.connection.servers", serverUrl,
                                    "quarkus.messaging.nats.connection.username", devServicesBuildTimeConfig.username(),
                                    "quarkus.messaging.nats.connection.password", devServicesBuildTimeConfig.password(),
                                    "quarkus.messaging.nats.connection.ssl-enabled",
                                    devServicesBuildTimeConfig.sslEnabled().toString()))
                            .build();
                });
    }

    private boolean natsDevServicesEnabled(DockerStatusBuildItem dockerStatusBuildItem,
            JetStreamDevServicesBuildTimeConfig devServicesConfig) {
        if (!devServicesConfig.enabled()) {
            // explicitly disabled
            log.debug("Not starting devservices for NATS as it has been disabled in the config");
            return false;
        }

        boolean needToStart = !ConfigUtils.isPropertyNonEmpty("quarkus.messaging.nats.connection.servers");
        if (!needToStart) {
            log.debug("Not starting dev services for NATS as servers have been provided");
            return false;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn("Please configure quarkus.messaging.nats.connection.servers for NATS or get a working docker instance");
            return false;
        }

        return true;
    }
}
