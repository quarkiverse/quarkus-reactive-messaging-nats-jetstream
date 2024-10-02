package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import io.nats.client.ErrorListener;
import io.nats.client.Options;

public class ConnectionOptionsFactory {

    public Options create(ConnectionConfiguration configuration,
            io.nats.client.ConnectionListener connectionListener)
            throws NoSuchAlgorithmException {
        final var servers = configuration.getServers().split(",");
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(0);
        if (connectionListener != null) {
            optionsBuilder.connectionListener(connectionListener);
        }
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

    public Options create(ConnectionConfiguration configuration)
            throws NoSuchAlgorithmException {
        return create(configuration, null);
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.getErrorListener()
                .orElseGet(DefaultErrorListener::new);
    }

}
