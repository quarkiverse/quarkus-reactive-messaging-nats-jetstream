package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;

import io.nats.client.ErrorListener;
import io.nats.client.Options;

public class ConnectionOptionsFactory {

    public Options create(ConnectionConfiguration configuration,
            io.nats.client.ConnectionListener connectionListener)
            throws NoSuchAlgorithmException {
        final var servers = configuration.servers().split(",");
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(servers);
        optionsBuilder.maxReconnects(0);
        if (connectionListener != null) {
            optionsBuilder.connectionListener(connectionListener);
        }
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.keystorePath().ifPresent(optionsBuilder::keystorePath);
        configuration.keystorePassword().map(String::toCharArray).ifPresent(optionsBuilder::keystorePassword);
        configuration.truststorePath().ifPresent(optionsBuilder::truststorePath);
        configuration.truststorePassword().map(String::toCharArray).ifPresent(optionsBuilder::truststorePassword);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout()
                .ifPresent(connectionTimeout -> optionsBuilder.connectionTimeout(Duration.ofMillis(connectionTimeout)));
        if (configuration.sslEnabled()) {
            optionsBuilder.opentls();
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        return optionsBuilder.build();
    }

    public Options create(ConnectionConfiguration configuration)
            throws NoSuchAlgorithmException {
        return create(configuration, null);
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(DefaultErrorListener::new);
    }

}
