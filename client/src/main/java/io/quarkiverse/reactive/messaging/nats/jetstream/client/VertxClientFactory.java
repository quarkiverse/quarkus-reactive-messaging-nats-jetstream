package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class VertxClientFactory implements ClientFactory {
    private final Vertx vertx;
    private final TracerFactory tracerFactory;

    @Override
    public Uni<Client> create(ClientConfiguration configuration) {
        return Uni.createFrom().<Client> item(Unchecked.supplier(() -> new VertxClient(
                configuration,
                Connection.of(Nats.connect(createConnectionOptions(configuration))),
                vertx.getOrCreateContext(),
                tracerFactory)))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    private Options createConnectionOptions(ClientConfiguration configuration) throws Exception {
        return createConnectionOptions(configuration.connectionConfiguration(), configuration.executorService());
    }

    private Options createConnectionOptions(ConnectionConfiguration configuration, ExecutorService executorService)
            throws Exception {
        final var optionsBuilder = new Options.Builder();
        optionsBuilder.servers(configuration.servers().toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.maximumReconnects().orElse(-1));
        optionsBuilder.connectionListener(ConnectionListener.of());
        optionsBuilder.errorListener(getErrorListener(configuration));
        configuration.username().ifPresent(username ->
                optionsBuilder.userInfo(username, configuration.password().orElse("")));
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

    private void runOnContext(Runnable action) {
        vertx.getOrCreateContext().runOnContext(action);
    }
}
