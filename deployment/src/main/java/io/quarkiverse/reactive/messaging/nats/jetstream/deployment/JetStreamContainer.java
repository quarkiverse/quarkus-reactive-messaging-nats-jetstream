package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import static io.quarkiverse.reactive.messaging.nats.jetstream.deployment.JetStreamDevServicesProcessor.DEV_SERVICE_LABEL;
import static io.quarkus.devservices.common.ConfigureUtil.configureSharedServiceLabel;

import java.time.Duration;
import java.util.OptionalInt;

import org.jboss.logging.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.deployment.builditem.Startable;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.runtime.LaunchMode;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class JetStreamContainer extends GenericContainer<JetStreamContainer> implements Startable {
    private static final Logger logger = Logger.getLogger(JetStreamContainer.class);

    static final Integer NATS_PORT = 4222;
    static final Integer NATS_HTTP_PORT = 8222;

    private final OptionalInt fixedExposedPort;
    private final boolean useSharedNetwork;

    private final String hostName;

    JetStreamContainer(DockerImageName imageName, OptionalInt fixedExposedPort, String defaultNetworkId,
            boolean useSharedNetwork, JetStreamConfiguration configuration) {
        super(imageName);

        super.withNetworkAliases("nats");
        super.waitingFor(new WaitAllStrategy()
                .withStrategy(Wait.forListeningPort())
                .withStrategy(Wait.forHttp("/healthz").forPort(NATS_HTTP_PORT).forStatusCode(200)));
        super.withStartupTimeout(Duration.ofSeconds(180L));
        super.withStartupAttempts(3);

        if (fixedExposedPort.isPresent()) {
            super.addFixedExposedPort(fixedExposedPort.getAsInt(), NATS_PORT);
        } else {
            addExposedPort(NATS_PORT);
        }
        addExposedPort(NATS_HTTP_PORT);

        if (configuration.sslEnabled()) {
            final String certificatePath = "/etc/nats/certs/server.crt";
            final String keyPath = "/etc/nats/certs/server.key";
            super.withFileSystemBind(configuration.certificateFile(), certificatePath, BindMode.READ_ONLY);
            super.withFileSystemBind(configuration.keyFile(), keyPath, BindMode.READ_ONLY);
            super.withCommand("--jetstream", "--user", configuration.username(), "--pass", configuration.password(),
                    "--http_port",
                    NATS_HTTP_PORT.toString(), "--tls", "--tlscert", certificatePath, "--tlskey", keyPath);
        } else {
            super.withCommand("--jetstream", "--user", configuration.username(), "--pass", configuration.password(),
                    "--http_port",
                    NATS_HTTP_PORT.toString());
        }

        super.withLogConsumer(outputFrame -> logger.info(outputFrame.getUtf8String().replace("\n", "")));

        this.fixedExposedPort = fixedExposedPort;
        this.useSharedNetwork = useSharedNetwork;
        this.hostName = ConfigureUtil.configureNetwork(this, defaultNetworkId, useSharedNetwork, "nats");
    }

    public JetStreamContainer withSharedServiceLabel(LaunchMode launchMode, String serviceName) {
        return configureSharedServiceLabel(this, launchMode, DEV_SERVICE_LABEL, serviceName);
    }

    @Override
    public String getHost() {
        return useSharedNetwork ? hostName : super.getHost();
    }

    public int getPort() {
        if (useSharedNetwork) {
            return NATS_PORT;
        }
        if (fixedExposedPort.isPresent()) {
            return fixedExposedPort.getAsInt();
        }
        return super.getFirstMappedPort();
    }

    public void close() {
        stop();
    }

    @Override
    public String getConnectionInfo() {
        return getHost() + ":" + getPort();
    }
}
