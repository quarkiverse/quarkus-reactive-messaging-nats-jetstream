package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.io.IOException;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.nats.client.JetStreamApiException;
import io.nats.client.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class KeyValueStore {
    private final PayloadMapper payloadMapper;

    @Inject
    public KeyValueStore(PayloadMapper payloadMapper) {
        this.payloadMapper = payloadMapper;
    }

    public <T> Uni<T> get(Connection connection, String bucketName, String key, Class<T> valueType) {
        return Uni.createFrom().item(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                return Optional.ofNullable(keyValue.get(key)).map(value -> payloadMapper.decode(value.getValue(), valueType))
                        .orElse(null);
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }).emitOn(runnable -> connection.context().runOnContext(runnable));
    }

    public <T> Uni<Void> put(Connection connection, String bucketName, String key, T value) {
        return Uni.createFrom().<Void> item(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.put(key, payloadMapper.toByteArray(value));
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }).emitOn(runnable -> connection.context().runOnContext(runnable));
    }

    public Uni<Void> delete(Connection connection, String bucketName, String key) {
        return Uni.createFrom().<Void> item(() -> {
            try {
                KeyValue keyValue = connection.keyValue(bucketName);
                keyValue.delete(key);
                return null;
            } catch (IOException | JetStreamApiException e) {
                throw new KeyValueException(e);
            }
        }).emitOn(runnable -> connection.context().runOnContext(runnable));
    }
}
