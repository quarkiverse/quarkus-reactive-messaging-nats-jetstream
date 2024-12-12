package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.IOException;

import io.nats.client.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import io.vertx.mutiny.core.Vertx;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultKeyValueStore<T> implements KeyValueStore<T> {
    private final String bucketName;
    private final io.nats.client.Connection connection;
    private final PayloadMapper payloadMapper;
    private final Vertx vertx;

    @Override
    public Uni<T> get(String key, Class<T> valueType) {
        return context().executeBlocking(Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                final var keyValue = connection.keyValue(bucketName);
                return keyValue.get(key);
            } catch (IOException failure) {
                throw new KeyValueException(failure);
            }
        }))
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().transform(keyValueEntry -> payloadMapper.of(keyValueEntry.getValue(), valueType)));
    }

    @Override
    public Uni<Void> put(String key, T value) {
        return context().executeBlocking(Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.put(key, payloadMapper.of(value));
                return null;
            } catch (Exception failure) {
                throw new KeyValueException(failure);
            }
        })));
    }

    @Override
    public Uni<Void> delete(String key) {
        return context().executeBlocking(Uni.createFrom().item(Unchecked.supplier(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.delete(key);
                return null;
            } catch (Exception failure) {
                throw new KeyValueException(failure);
            }
        })));
    }

    private Context context() {
        return vertx.getOrCreateContext();
    }
}
