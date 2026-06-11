package io.quarkiverse.reactive.messaging.nats.jetstream;

import io.nats.client.Nats;
import io.nats.client.Options;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.ErrorListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.connection.NativeConnectionDelegate;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.Operation;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing.TracerFactory;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

import static io.nats.client.Options.DEFAULT_RECONNECT_WAIT;

@RequiredArgsConstructor
public class VertxClientFactory implements ClientFactory {
    private final Vertx vertx;
    private final TracerFactory tracerFactory;

    @Override
    public Uni<Client> create(ClientConfiguration configuration) {
        return Uni.createFrom().<Client>item(Unchecked.supplier(() -> new VertxClient(
                        configuration,
                        new NativeConnectionDelegate(Nats.connect(createConnectionOptions(configuration))),
                        vertx.getOrCreateContext(),
                        tracerFactory.create(Operation.PUBLISH))))
                .runSubscriptionOn(configuration.executorService())
                .emitOn(this::runOnContext);
    }

    private Options createConnectionOptions(ClientConfiguration configuration) throws Exception {
        return createConnectionOptions(configuration.connection(), configuration.executorService());
    }

    private Options createConnectionOptions(ConnectionConfiguration configuration, ExecutorService executorService) throws Exception {
        final var optionsBuilder = new Options.Builder();
        final var servers = configuration.servers();
        optionsBuilder.servers(servers.toArray(new String[0]));
        optionsBuilder.maxReconnects(configuration.connectionAttempts().orElse(-1));
        optionsBuilder.connectionTimeout(configuration.connectionBackoff().orElse(DEFAULT_RECONNECT_WAIT));

        optionsBuilder.connectionListener(ConnectionListener.of());
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
