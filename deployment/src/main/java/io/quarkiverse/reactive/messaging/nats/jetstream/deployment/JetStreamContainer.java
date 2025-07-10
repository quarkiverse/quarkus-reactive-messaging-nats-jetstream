package io.quarkiverse.reactive.messaging.nats.jetstream.deployment;

import java.time.Duration;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

class JetStreamContainer extends GenericContainer<JetStreamContainer> {
    private static final Logger logger = Logger.getLogger(JetStreamContainer.class);
    public static final DockerImageName NATS_IMAGE = DockerImageName.parse("nats:2.11");

    static final Integer NATS_PORT = 4222;
    static final Integer NATS_HTTP_PORT = 8222;

    static final String USERNAME = "guest";
    static final String PASSWORD = "guest";

    JetStreamContainer(DockerImageName imageName) {
        super(imageName);

        super.withNetworkAliases("nats");
        super.waitingFor(Wait.forHttp("/healthz").forPort(NATS_HTTP_PORT));
        super.withStartupTimeout(Duration.ofSeconds(180L));
        super.withExposedPorts(NATS_PORT, NATS_HTTP_PORT);
        super.withCommand("--jetstream", "--user", USERNAME, "--pass", PASSWORD, "--http_port", NATS_HTTP_PORT.toString());
        super.withLogConsumer(outputFrame -> logger.info(outputFrame.getUtf8String().replace("\n", "")));
    }

    String getServerUrl() {
        return String.format("nats://%s:%s", getHost(), getMappedPort(NATS_PORT));
    }

    JetStreamContainer withPort(final int fixedPort) {
        if (fixedPort <= 0) {
            throw new IllegalArgumentException("The fixed port must be greater than 0");
        }
        addFixedExposedPort(fixedPort, NATS_PORT);
        return self();
    }
}
