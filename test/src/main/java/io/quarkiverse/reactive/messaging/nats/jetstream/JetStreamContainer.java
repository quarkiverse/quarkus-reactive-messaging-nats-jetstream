package io.quarkiverse.reactive.messaging.nats.jetstream;

import static io.quarkus.devservices.common.ConfigureUtil.configureNetwork;

import java.time.Duration;
import java.util.Optional;

import org.jboss.logging.Logger;
import org.jspecify.annotations.NonNull;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class JetStreamContainer extends GenericContainer<JetStreamContainer> {
    private static final Logger log = Logger.getLogger(JetStreamContainer.class);

    public static final Integer NATS_PORT = 4222;
    public static final Integer NATS_HTTP_PORT = 8222;

    private final Optional<Integer> fixedExposedPort;
    private final boolean useSharedNetwork;

    private final String hostName;

    private final JetStreamContainerConfiguration configuration;

    public JetStreamContainer(Integer fixedExposedPort, String defaultNetworkId,
            boolean useSharedNetwork, JetStreamContainerConfiguration configuration) {
        this(DockerImageName.parse("nats:2.14"), fixedExposedPort, defaultNetworkId, useSharedNetwork, configuration);
    }

    public JetStreamContainer(DockerImageName imageName, Integer fixedExposedPort, String defaultNetworkId,
            boolean useSharedNetwork, JetStreamContainerConfiguration configuration) {
        super(imageName);

        this.fixedExposedPort = Optional.ofNullable(fixedExposedPort);
        this.useSharedNetwork = useSharedNetwork;
        this.configuration = configuration;

        super.withNetworkAliases("nats");
        super.waitingFor(new WaitAllStrategy()
                .withStrategy(Wait.forHttp("/healthz").forPort(NATS_HTTP_PORT).forStatusCode(200)));
        super.withStartupTimeout(Duration.ofSeconds(180L));
        super.withStartupAttempts(3);

        if (this.fixedExposedPort.isPresent()) {
            super.addFixedExposedPort(this.fixedExposedPort.get(), NATS_PORT);
        } else {
            addExposedPort(NATS_PORT);
        }
        addExposedPort(NATS_HTTP_PORT);

        if (configuration.sslEnabled()) {
            final String certificatePath = "/etc/nats/certs/server.crt";
            final String keyPath = "/etc/nats/certs/server.key";
            super.withFileSystemBind(
                    configuration.certificateFile().orElseThrow(
                            () -> new IllegalArgumentException("Certificate file is required when SSL is enabled")),
                    certificatePath, BindMode.READ_ONLY);
            super.withFileSystemBind(
                    configuration.keyFile()
                            .orElseThrow(() -> new IllegalArgumentException("Key file is required when SSL is enabled")),
                    keyPath, BindMode.READ_ONLY);
            super.withCommand("--jetstream", "--user", configuration.username(), "--pass", configuration.password(),
                    "--http_port",
                    NATS_HTTP_PORT.toString(), "--tls", "--tlscert", certificatePath, "--tlskey", keyPath);
        } else {
            super.withCommand("--jetstream", "--user", configuration.username(), "--pass", configuration.password(),
                    "--http_port",
                    NATS_HTTP_PORT.toString());
        }

        super.withLogConsumer(outputFrame -> log.info(outputFrame.getUtf8String().replace("\n", "")));

        this.hostName = configureNetwork(this, defaultNetworkId, useSharedNetwork, "nats");
    }

    @Override
    public String getHost() {
        return useSharedNetwork ? hostName : super.getHost();
    }

    public int getPort() {
        if (useSharedNetwork) {
            return NATS_PORT;
        }
        return fixedExposedPort.orElseGet(super::getFirstMappedPort);
    }

    public void close() {
        stop();
    }

    public String getConnectionInfo() {
        return getHost() + ":" + getPort();
    }

    public @NonNull JetStreamContainerConfiguration getConfiguration() {
        return configuration;
    }
}
