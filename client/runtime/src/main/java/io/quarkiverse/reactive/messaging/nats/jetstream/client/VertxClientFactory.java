package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VertxClientFactory extends AbstractClientFactory {
    private final Vertx vertx;
    private final TracerFactory tracerFactory;

    @Override
    @NonNull
    Client create(@NonNull final NativeConnection connection,
            @NonNull final ClientThreadPool threadPool,
            @NonNull Serializer serializer) {
        return new ClientImpl(
                connection,
                new VertxContext(vertx.getOrCreateContext(), threadPool.executorService()),
                threadPool,
                tracerFactory,
                serializer);
    }

}
