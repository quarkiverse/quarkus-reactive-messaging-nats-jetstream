package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.io.DefaultErrorListener;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

public class JetStreamClient implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(JetStreamClient.class);
    private final ConnectionConfiguration configuration;
    private final Vertx vertx;
    private final AtomicReference<Connection> connection;
    private final AtomicReference<List<ConnectionListener>> listeners;

    public JetStreamClient(ConnectionConfiguration configuration, Vertx vertx) {
        this.vertx = vertx;
        this.configuration = configuration;
        this.connection = new AtomicReference<>();
        this.listeners = new AtomicReference<>(List.of());
    }

    public JetStreamClient(ConnectionConfiguration configuration) {
        this(configuration, null);
    }

    public Uni<Connection> getOrEstablishConnection() {
        return Uni.createFrom().item(() -> Optional.ofNullable(connection.get())
                .filter(this::isConnected)
                .orElse(null))
                .onItem().ifNull().switchTo(this::connect)
                .onItem().invoke(this.connection::set);
    }

    public Optional<Connection> getConnection() {
        return Optional.ofNullable(connection.get());
    }

    public Optional<Vertx> getVertx() {
        return Optional.ofNullable(vertx);
    }

    public void addListener(ConnectionListener listener) {
        listeners.updateAndGet(connectionListeners -> {
            final var result = new ArrayList<>(connectionListeners);
            result.add(listener);
            return result;
        });
    }

    private void fireEvent(ConnectionEvent event, String message) {
        if (!(ConnectionEvent.Connected.equals(event) || ConnectionEvent.Closed.equals(event))) {
            logger.warnf("Fire event: %s with message: %s", event, message);
        }
        listeners.get().forEach(listener -> listener.onEvent(event, message));
    }

    private Uni<Connection> connect() {
        return getVertx().map(v -> connectWithContext(v.getOrCreateContext())).orElseGet(this::connectWithoutContext);
    }

    private Uni<Connection> connectWithContext(Context context) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var options = createConnectionOptions(configuration, new InternalConnectionListener());
                return new Connection(Nats.connectReconnectOnConnect(options), context);
            } catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        })).emitOn(context::runOnContext);
    }

    private Uni<Connection> connectWithoutContext() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var options = createConnectionOptions(configuration, new InternalConnectionListener());
                return new Connection(Nats.connectReconnectOnConnect(options), null);
            } catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private boolean isConnected(Connection connection) {
        return connection != null && connection.isConnected();
    }

    private Options createConnectionOptions(ConnectionConfiguration configuration,
            io.nats.client.ConnectionListener connectionListener)
            throws NoSuchAlgorithmException {
        final var servers = configuration.getServers().split(",");
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(configuration.getMaxReconnects().orElse(-1));
        optionsBuilder.connectionListener(connectionListener);
        optionsBuilder.errorListener(getErrorListener(configuration));
        if (configuration.getUsername().isPresent()) {
            optionsBuilder.userInfo(configuration.getUsername().get(), configuration.getPassword().orElse(""));
        }
        if (configuration.getToken().isPresent()) {
            optionsBuilder.token(configuration.getToken().get());
        }
        configuration.getBufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.getConnectionTimeout()
                .ifPresent(connectionTimeout -> optionsBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout)));
        if (configuration.sslEnabled()) {
            optionsBuilder.opentls();
        }
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.getErrorListener()
                .orElseGet(DefaultErrorListener::new);
    }

    @Override
    public void close() {
        if (connection.get() != null) {
            connection.getAndSet(null).close();
        }
    }

    private class InternalConnectionListener implements io.nats.client.ConnectionListener {
        @Override
        public void connectionEvent(io.nats.client.Connection conn, Events type) {
            switch (type) {
                case CONNECTED:
                    fireEvent(ConnectionEvent.Connected, "Connection established");
                    break;
                case DISCONNECTED:
                    fireEvent(ConnectionEvent.Disconnected, "Conection disconnected");
                    break;
                case CLOSED:
                    fireEvent(ConnectionEvent.Closed, "Connection closed");
                    break;
                case RECONNECTED:
                    fireEvent(ConnectionEvent.Reconnected, "Connection restored");
                    break;
                case RESUBSCRIBED:
                    fireEvent(ConnectionEvent.Resubscribed, "Resubscribed");
                    break;
                case DISCOVERED_SERVERS:
                    fireEvent(ConnectionEvent.DiscoveredServers, "Discovered servers");
                    break;
                case LAME_DUCK:
                    fireEvent(ConnectionEvent.LameDuck, "Lame duck");
                    break;
                default:
                    throw new RuntimeException(String.format("Unknown event type: %s", type));
            }
        }
    }
}
