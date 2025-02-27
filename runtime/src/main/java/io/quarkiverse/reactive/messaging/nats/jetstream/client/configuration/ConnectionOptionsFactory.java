package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.ErrorListener;
import io.nats.client.Options;
import io.quarkus.tls.*;

public class ConnectionOptionsFactory {
    public static final int DEFAULT_MAX_RECONNECT = -1;

    public Options create(final ConnectionConfiguration configuration,
            final io.nats.client.ConnectionListener connectionListener,
            final TlsConfigurationRegistry tlsConfigurationRegistry)
            throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers().split(",");
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(configuration.connectionAttempts().orElse(DEFAULT_MAX_RECONNECT));
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));
        if (connectionListener != null) {
            optionsBuilder.connectionListener(connectionListener);
        }
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout()
                .ifPresent(connectionTimeout -> optionsBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout)));
        if (configuration.sslEnabled()) {
            optionsBuilder.opentls();

            final Optional<TlsConfiguration> tlsConfiguration;
            if (configuration.tlsConfigurationName().isPresent()) {
                tlsConfiguration = tlsConfigurationRegistry.get(configuration.tlsConfigurationName().get());
            } else {
                tlsConfiguration = tlsConfigurationRegistry.getDefault();
            }

            if (tlsConfiguration.isEmpty()) {
                throw new IllegalStateException("No Quarkus TLS configuration found for NATS JetStream connection");
            } else {
                optionsBuilder.sslContext(tlsConfiguration.get().createSSLContext());
            }
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(DefaultErrorListener::new);
    }
}
