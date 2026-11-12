package io.quarkiverse.reactive.messaging.nats.jetstream.connector.test.health;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;

import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * A NATS server bound to a fixed host port, so it can be stopped and started again at the same address the
 * application is connected to. Its JetStream storage survives a restart, like a restarted NATS pod with a volume.
 * <p>
 * The test class is loaded by more than one class loader, so the server is shared through system properties to make
 * sure the application and the test use the same container.
 */
final class RestartableNatsServer {
    private static final int NATS_PORT = 4222;

    private final String key;

    private RestartableNatsServer(String key) {
        this.key = key;
    }

    static synchronized RestartableNatsServer shared(String key) {
        final var server = new RestartableNatsServer(key);
        if (System.getProperty(server.idProperty()) == null) {
            final var port = freePort();
            @SuppressWarnings("resource")
            final var container = new FixedPortContainer(port);
            container.start();
            System.setProperty(server.idProperty(), container.getContainerId());
            System.setProperty(server.portProperty(), String.valueOf(port));
        }
        return server;
    }

    String url() {
        return "nats://localhost:" + System.getProperty(portProperty());
    }

    void stopServer() {
        DockerClientFactory.instance().client().stopContainerCmd(id()).exec();
    }

    void startServer() {
        DockerClientFactory.instance().client().startContainerCmd(id()).exec();
    }

    synchronized void remove() {
        final var id = System.getProperty(idProperty());
        if (id != null) {
            DockerClientFactory.instance().client().removeContainerCmd(id).withForce(true).exec();
            System.clearProperty(idProperty());
            System.clearProperty(portProperty());
        }
    }

    private String id() {
        return System.getProperty(idProperty());
    }

    private String idProperty() {
        return "test.nats." + key + ".id";
    }

    private String portProperty() {
        return "test.nats." + key + ".port";
    }

    private static int freePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final class FixedPortContainer extends GenericContainer<FixedPortContainer> {
        FixedPortContainer(int hostPort) {
            super(DockerImageName.parse("nats:2.15"));
            addFixedExposedPort(hostPort, NATS_PORT);
            withCommand("--jetstream");
            waitingFor(Wait.forLogMessage(".*Server is ready.*", 1));
        }
    }
}
