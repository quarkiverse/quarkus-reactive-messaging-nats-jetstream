package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

import java.util.concurrent.atomic.AtomicReference;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkus.tls.TlsConfiguration;
import io.quarkus.tls.TlsConfigurationRegistry;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor
public class ConnectionFactoryImpl implements ConnectionFactory {
    private final ConnectionConfiguration configuration;
    private final TlsConfigurationRegistry tlsConfigurationRegistry;
    private final AtomicReference<Connection> connection = new AtomicReference<>();

    @ApplicationScoped
    @Produces
    @Override
    public Connection create() {
        try {
            final var options = createOptions();
            return new ConnectionImpl(Nats.connect(options));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Options createOptions() throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.connectionAttempts());
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));

        optionsBuilder.connectionListener(new NativeConnectionListener());
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout().ifPresent(optionsBuilder::connectionTimeout);
        if (configuration.sslEnabled().orElse(false)) {
            optionsBuilder.opentls();
            final var tlsConfiguration = configuration.tlsConfigurationName()
                    .flatMap(tlsConfigurationRegistry::get)
                    .orElseGet(() -> getDefaultTlsConfiguration(tlsConfigurationRegistry));
            optionsBuilder.sslContext(tlsConfiguration.createSSLContext());
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(NativeErrorListener::new);
    }

    private TlsConfiguration getDefaultTlsConfiguration(TlsConfigurationRegistry tlsConfigurationRegistry) {
        return tlsConfigurationRegistry.getDefault().orElseThrow(
                () -> new IllegalStateException("No Quarkus TLS configuration found for NATS JetStream connection"));
    }
}
