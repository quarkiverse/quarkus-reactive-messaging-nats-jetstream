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

    public Optional<Boolean> isConnected() {
        return Optional.ofNullable(connection.get()).map(Connection::isConnected);
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
        logger.debugf("Connection event: %s with message: %s", event, message);
        listeners.get().forEach(listener -> listener.onEvent(event, message));
    }

    private Uni<Connection> connect() {
        return getContext().map(this::connectWithContext).orElseGet(this::connectWithoutContext);
    }

    private Uni<Connection> connectWithContext(Context context) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var options = createConnectionOptions(configuration, new InternalConnectionListener(context));
                return new Connection(Nats.connect(options), context);
            } catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        })).emitOn(context::runOnContext);
    }

    private Uni<Connection> connectWithoutContext() {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var options = createConnectionOptions(configuration, new InternalConnectionListener(null));
                return new Connection(Nats.connect(options), null);
            } catch (NoSuchAlgorithmException | IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));
    }

    private boolean isConnected(Connection connection) {
        return connection != null && connection.isConnected();
    }

    private Optional<Context> getContext() {
        return getVertx().map(Vertx::getOrCreateContext);
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
        configuration.getUsername()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.getPassword().orElse("")));
        configuration.getToken().ifPresent(optionsBuilder::token);
        configuration.getCredentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.getKeystorePath().ifPresent(optionsBuilder::keystorePath);
        configuration.getKeystorePassword().map(String::toCharArray).ifPresent(optionsBuilder::keystorePassword);
        configuration.getTruststorePath().ifPresent(optionsBuilder::truststorePath);
        configuration.getKeystorePassword().map(String::toCharArray).ifPresent(optionsBuilder::truststorePassword);
        configuration.getBufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.getConnectionTimeout()
                .ifPresent(connectionTimeout -> optionsBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout)));
        if (configuration.sslEnabled()) {
            optionsBuilder.opentls();
        }
        configuration.getTlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.getErrorListener()
                .orElseGet(DefaultErrorListener::new);
    }

    private void setConnection(Connection connection) {
        this.connection.set(connection);
    }

    @Override
    public void close() {
        if (connection.get() != null) {
            connection.getAndSet(null).close();
        }
    }

    private class InternalConnectionListener implements io.nats.client.ConnectionListener {
        private final Context context;

        public InternalConnectionListener(Context context) {
            this.context = context;
        }

        @Override
        public void connectionEvent(io.nats.client.Connection connection, Events type) {
            switch (connection.getStatus()) {
                case CONNECTED -> {
                    setConnection(new Connection(connection, context));
                    fireEvent(ConnectionEvent.Connected, "Connection established");
                }
                case CONNECTING -> fireEvent(ConnectionEvent.Connecting, "Connecting to server");
                case CLOSED -> fireEvent(ConnectionEvent.Closed, "Connection closed");
                case RECONNECTING -> fireEvent(ConnectionEvent.Reconnecting, "Reconnecting to server");
                case DISCONNECTED -> fireEvent(ConnectionEvent.Disconnected, "Connection disconnected");
            }
        }
    }
}
