package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageMapper;
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
    Client create(@NonNull final NativeConnection connection, @NonNull Serializer serializer,
            @NonNull final ExecutorService executorService) {
        return new ClientImpl(
                connection,
                new VertxClientContext(vertx.getOrCreateContext(), executorService),
                tracerFactory,
                MessageMapper.of(serializer));
    }

}
