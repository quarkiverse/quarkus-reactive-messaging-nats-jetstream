package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;

@ApplicationScoped
@RequiredArgsConstructor
@JBossLog
public class ConnectionFactoryImpl implements ConnectionFactory {
    private final ConnectorConfiguration configuration;
    private final TlsContext tlsContext;
    private final AtomicReference<Connection> connection = new AtomicReference<>();

    @Produces
    @Override
    public Connection create() {
        return connection.updateAndGet(c -> c != null && c.isConnected() ? c : createNewConnection());
    }

    private Connection createNewConnection() {
        try {
            final var options = createOptions();
            return new ConnectionImpl(Nats.connect(options));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PreDestroy
    public void closeConnection() {
        final var connection = this.connection.getAndSet(null);
        if (connection != null && connection.isConnected()) {
            try {
                connection.close();
            } catch (Exception e) {
                log.warn("Failed to close NATS connection", e);
            }
        }
    }

    private ConnectionConfiguration configuration() {
        return configuration.connection();
    }

    private Options createOptions() throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration().servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration().connectionAttempts());
        optionsBuilder.connectionTimeout(configuration().connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));

        optionsBuilder.connectionListener(new NativeConnectionListener());
        optionsBuilder.errorListener(getErrorListener(configuration()));
        configuration().username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration().password().orElse("")));
        configuration().token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration().credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration().bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration().connectionTimeout().ifPresent(optionsBuilder::connectionTimeout);
        if (tlsContext.sslContext().isPresent()) {
            optionsBuilder.opentls();
            optionsBuilder.sslContext(tlsContext.sslContext().get());
        }
        configuration().tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(NativeErrorListener::new);
    }

}
