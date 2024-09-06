package io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates;

import static io.nats.client.Connection.Status.CONNECTED;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.jboss.logging.Logger;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

public class ConnectionDelegate {
    private final static Logger logger = Logger.getLogger(ConnectionDelegate.class);

    public io.nats.client.Connection connect(Connection connection, ConnectionConfiguration configuration)
            throws ConnectionException {
        try {
            final var options = createConnectionOptions(configuration, new InternalConnectionListener(connection));
            return Nats.connect(options);
        } catch (Throwable failure) {
            throw new ConnectionException(failure);
        }
    }

    public Uni<Void> flush(Supplier<io.nats.client.Connection> connection, Duration duration) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                connection.get().flush(duration);
                return null;
            } catch (TimeoutException | InterruptedException e) {
                throw new ConnectionException(e);
            }
        }));
    }

    public void close(Supplier<io.nats.client.Connection> connection) {
        try {
            connection.get().close();
        } catch (Throwable throwable) {
            logger.warnf(throwable, "Could not close connection: %s", throwable.getMessage());
        }
    }

    public boolean isConnected(Supplier<io.nats.client.Connection> connection) {
        return CONNECTED.equals(connection.get().getStatus());
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
