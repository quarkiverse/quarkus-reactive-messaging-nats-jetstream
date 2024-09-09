package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import static io.nats.client.Connection.Status.CONNECTED;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.jboss.logging.Logger;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

public abstract class AbstractConnection implements Connection {
    private final static Logger logger = Logger.getLogger(AbstractConnection.class);

    protected final io.nats.client.Connection connection;
    private final List<ConnectionListener> listeners;

    public AbstractConnection(ConnectionConfiguration connectionConfiguration,
            ConnectionListener connectionListener) {
        this.connection = connect(connectionConfiguration);
        this.listeners = new ArrayList<>(List.of(connectionListener));
    }

    @Override
    public boolean isConnected() {
        return CONNECTED.equals(connection.getStatus());
    }

    @Override
    public Uni<Void> flush(Duration duration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                connection.flush(duration);
                return null;
            } catch (TimeoutException | InterruptedException e) {
                throw new ConnectionException(e);
            }
        }));
    }

    @Override
    public List<ConnectionListener> listeners() {
        return listeners;
    }

    @Override
    public void addListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws Exception {
        try {
            connection.close();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Error closing connection: %s", throwable.getMessage());
        }
    }

    private io.nats.client.Connection connect(ConnectionConfiguration configuration) throws ConnectionException {
        try {
            final var options = createConnectionOptions(configuration, new InternalConnectionListener(this));
            return Nats.connect(options);
        } catch (Throwable failure) {
            throw new ConnectionException(failure);
        }
    }

    private Options createConnectionOptions(ConnectionConfiguration configuration,
            io.nats.client.ConnectionListener connectionListener)
            throws NoSuchAlgorithmException {
        final var servers = configuration.getServers().split(",");
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(0);
        optionsBuilder.connectionListener(connectionListener);
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.getUsername()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.getPassword().orElse("")));
        configuration.getToken().map(String::toCharArray).ifPresent(optionsBuilder::token);
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
}
