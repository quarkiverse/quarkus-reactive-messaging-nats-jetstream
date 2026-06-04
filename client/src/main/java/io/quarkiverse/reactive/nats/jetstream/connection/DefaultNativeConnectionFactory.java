package io.quarkiverse.reactive.nats.jetstream.connection;

import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

class DefaultNativeConnectionFactory implements NativeConnectionFactory {

    @Override
    public NativeConnection create(ConnectionConfiguration configuration) {
        try {
            final var options = createOptions(configuration);
            return new NativeConnectionDelegate(Nats.connect(options));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Options createOptions(ConnectionConfiguration configuration) throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.connectionAttempts().orElse(-1));
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));

        optionsBuilder.connectionListener(new NativeConnectionListener());
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username()
                .ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.connectionTimeout().ifPresent(optionsBuilder::connectionTimeout);
        if (configuration.sslContext().isPresent()) {
            optionsBuilder.opentls();
            optionsBuilder.sslContext(configuration.sslContext().get());
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        optionsBuilder.executor(configuration.executorService());
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(NativeErrorListener::new);
    }
}
