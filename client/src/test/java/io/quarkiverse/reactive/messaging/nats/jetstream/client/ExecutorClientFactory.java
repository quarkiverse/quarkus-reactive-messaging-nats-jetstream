package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Serializer;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing.TracerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExecutorClientFactory extends AbstractClientFactory {
    private final TracerFactory tracerFactory;

    @Override
    @NonNull
    Client create(@NonNull NativeConnection connection, @NonNull Serializer serializer,
            @NonNull ExecutorService executorService) {
        return new ClientImpl(
                connection,
                new ExecutorClientContext(executorService),
                tracerFactory,
                MessageMapper.of(serializer));
    }

}
