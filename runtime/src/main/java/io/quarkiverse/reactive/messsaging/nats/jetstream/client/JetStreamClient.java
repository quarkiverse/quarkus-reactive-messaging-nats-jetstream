package io.quarkiverse.reactive.messsaging.nats.jetstream.client;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.nats.client.ConnectionListener;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;

public class JetStreamClient implements AutoCloseable {
    private final ConnectionConfiguration configuration;
    private final Vertx vertx;
    private final AtomicReference<Connection> connection;

    public JetStreamClient(ConnectionConfiguration configuration, Vertx vertx) {
        this.vertx = vertx;
        this.configuration = configuration;
        this.connection = new AtomicReference<>();
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

    private Uni<Connection> connect() {
        return getVertx().map(v -> connectWithContext(v.getOrCreateContext())).orElseGet(this::connectWithoutContext);
    }

    private Uni<Connection> connectWithContext(Context context) {
        return Uni.createFrom().<Connection> emitter(em -> {
            try {
                final var options = createConnectionOptions(configuration, (connection, type) -> {
                    if (ConnectionListener.Events.CONNECTED.equals(type)) {
                        em.complete(new Connection(connection, context));
                    }
                });
                Nats.connectAsynchronously(options, true);
            } catch (InterruptedException | NoSuchAlgorithmException e) {
                em.fail(e);
            }
        }).emitOn(context::runOnContext);
    }

    private Uni<Connection> connectWithoutContext() {
        return Uni.createFrom().emitter(em -> {
            try {
                final var options = createConnectionOptions(configuration, (connection, type) -> {
                    if (ConnectionListener.Events.CONNECTED.equals(type)) {
                        em.complete(new Connection(connection, null));
                    }
                });
                Nats.connectAsynchronously(options, true);
            } catch (InterruptedException | NoSuchAlgorithmException e) {
                em.fail(e);
            }
        });
    }

    private boolean isConnected(Connection connection) {
        return connection != null && connection.isConnected();
    }

    private Options createConnectionOptions(ConnectionConfiguration configuration, ConnectionListener connectionListener)
            throws NoSuchAlgorithmException {
        final var servers = configuration.getServers().split(",");
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(configuration.getMaxReconnects().orElse(-1));
        optionsBuilder.connectionListener(connectionListener);
        optionsBuilder.errorListener(getErrorListener(configuration));
        optionsBuilder.userInfo(configuration.getUsername().orElse(null), configuration.getPassword().orElse(null));
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
            connection.get().close();
        }
        connection.set(null);
    }
}
