package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;

abstract class AbstractClientFactory implements ClientFactory {

    @Override
    public @NonNull Client create(@NonNull final ConnectionConfiguration configuration,
            @NonNull Serializer serializer,
            @NonNull final ExecutorService executorService) throws ClientException {
        try {
            return create(NativeConnection.of(Nats.connect(createConnectionOptions(configuration, executorService))),
                    serializer,
                    executorService);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    abstract @NonNull Client create(@NonNull final NativeConnection connection, @NonNull Serializer serializer,
            @NonNull final ExecutorService executorService);

    private Options createConnectionOptions(ConnectionConfiguration configuration, ExecutorService executorService)
            throws Exception {
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(configuration.servers().toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.maximumReconnects());
        optionsBuilder.connectionListener(ConnectionListener.of());
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username().ifPresent(username -> optionsBuilder.userInfo(username, configuration.password().orElse("")));
        configuration.token().map(String::toCharArray).ifPresent(optionsBuilder::token);
        configuration.credentialPath().ifPresent(optionsBuilder::credentialPath);
        configuration.bufferSize().ifPresent(optionsBuilder::bufferSize);
        configuration.timeout().ifPresent(optionsBuilder::connectionTimeout);
        if (configuration.sslContext().isPresent()) {
            optionsBuilder.opentls();
            optionsBuilder.sslContext(configuration.sslContext().get());
        }
        configuration.tlsAlgorithm().ifPresent(optionsBuilder::tlsAlgorithm);
        optionsBuilder.executor(executorService);
        return optionsBuilder.build();
    }

    private ErrorListener getErrorListener(ConnectionConfiguration configuration) {
        return configuration.errorListener()
                .orElseGet(ErrorListener::of);
    }
}
