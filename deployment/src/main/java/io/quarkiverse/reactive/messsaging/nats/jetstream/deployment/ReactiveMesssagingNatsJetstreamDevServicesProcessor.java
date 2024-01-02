package io.quarkiverse.reactive.messsaging.nats.jetstream.deployment;

import static io.quarkiverse.reactive.messsaging.nats.jetstream.deployment.ReactiveMesssagingNatsJetstreamProcessor.FEATURE;

import java.io.Closeable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.LaunchMode;

/**
 * Starts a NATS JetStream broker as dev service if needed.
 * It uses https://hub.docker.com/_/nats as image.
 */
@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class ReactiveMesssagingNatsJetstreamDevServicesProcessor {
    private static final Logger logger = Logger.getLogger(ReactiveMesssagingNatsJetstreamDevServicesProcessor.class);

    /**
     * Label to add to shared Dev Service for pulsar running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    private static final String DEV_SERVICE_LABEL = "quarkus-dev-service-jetstream";

    private static final ContainerLocator jetStreamContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            NatsJetStreamContainer.NATS_PORT);

    static volatile RunningDevService devService;
    static volatile boolean first = true;
    static volatile JetStreamDevServiceCfg cfg;

    @BuildStep
    public DevServicesResultBuildItem startJetStreamDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            ReactiveMesssagingNatsJetstreamDevServicesBuildTimeConfig devServicesBuildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig) {

        JetStreamDevServiceCfg configuration = new JetStreamDevServiceCfg(devServicesBuildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdownBroker();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "NATS JetStream Dev Services Starting:", consoleInstalledBuildItem,
                loggingSetupBuildItem);
        try {
            DevServicesResultBuildItem.RunningDevService newDevService = startJetStreamContainer(dockerStatusBuildItem,
                    configuration, launchMode,
                    devServicesConfig.timeout);
            if (newDevService != null) {
                devService = newDevService;
                if (newDevService.isOwner()) {
                    logger.info("Dev Services for NATS JetStream started.");
                }
            }
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownBroker();

                    logger.info("Dev Services for NATS JetStream shut down.");
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;
        return devService.toBuildItem();
    }

    private void shutdownBroker() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                logger.error("Failed to stop the NATS JetStream broker", e);
            } finally {
                devService = null;
            }
        }
    }

    private RunningDevService startJetStreamContainer(DockerStatusBuildItem dockerStatusBuildItem,
            JetStreamDevServiceCfg config,
            LaunchModeBuildItem launchMode, Optional<Duration> timeout) {
        if (!config.devServicesEnabled) {
            // explicitly disabled
            logger.debug("Not starting Dev Services for NATS JetStream, as it has been disabled in the config.");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            logger.warn("Docker isn't working, please configure the NATS JetStream broker location.");
            return null;
        }

        final Supplier<RunningDevService> defaultJetStreamBrokerSupplier = () -> {
            // Starting the broker
            NatsJetStreamContainer container = new NatsJetStreamContainer(DockerImageName.parse(config.imageName)
                    .asCompatibleSubstituteFor("nats"))
                    .withNetwork(Network.SHARED);
            if (launchMode.getLaunchMode() == LaunchMode.DEVELOPMENT) { // Only adds the label in dev mode.
                container.withLabel(DEV_SERVICE_LABEL, config.serviceName);
            }
            if (config.fixedExposedPort != 0) {
                container.withPort(config.fixedExposedPort);
            }
            timeout.ifPresent(container::withStartupTimeout);
            container.start();

            return getRunningService(container.getContainerId(), container::close, container.getServerUrl());
        };

        return jetStreamContainerLocator.locateContainer(config.serviceName, config.shared, launchMode.getLaunchMode())
                .map(containerAddress -> getRunningService(containerAddress.getId(), null,
                        String.format("nats://%s:%s", containerAddress.getHost(), containerAddress.getPort())))
                .orElseGet(defaultJetStreamBrokerSupplier);
    }

    private RunningDevService getRunningService(String containerId, Closeable closeable, String serverUrl) {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("quarkus.reactive-messaging.nats.servers", serverUrl);
        configMap.put("quarkus.reactive-messaging.nats.username", NatsJetStreamContainer.USERNAME);
        configMap.put("quarkus.reactive-messaging.nats.password", NatsJetStreamContainer.PASSWORD);
        configMap.put("quarkus.reactive-messaging.nats.ssl-enabled", "false");
        return new RunningDevService(FEATURE, containerId, closeable, configMap);
    }

    private static final class JetStreamDevServiceCfg {
        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final String serviceName;

        public JetStreamDevServiceCfg(ReactiveMesssagingNatsJetstreamDevServicesBuildTimeConfig devServicesConfig) {
            this.devServicesEnabled = devServicesConfig.enabled().orElse(true);
            this.imageName = devServicesConfig.imageName();
            this.fixedExposedPort = devServicesConfig.port().orElse(0);
            this.shared = devServicesConfig.shared();
            this.serviceName = devServicesConfig.serviceName();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            JetStreamDevServiceCfg that = (JetStreamDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && shared == that.shared
                    && Objects.equals(imageName, that.imageName) && Objects.equals(fixedExposedPort, that.fixedExposedPort)
                    && Objects.equals(serviceName, that.serviceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort, shared, serviceName);
        }
    }
}
