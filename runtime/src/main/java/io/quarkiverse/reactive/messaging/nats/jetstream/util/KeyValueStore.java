package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import io.quarkiverse.reactive.messaging.nats.jetstream.mapper.PayloadMapper;
import jakarta.enterprise.context.ApplicationScoped;

import io.nats.client.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;

@ApplicationScoped
public class KeyValueStore {
    private final PayloadMapper payloadMapper;

    @Inject
    public KeyValueStore(PayloadMapper payloadMapper) {
        this.payloadMapper = payloadMapper;
    }

    public <T> Uni<T> getValue(Connection connection, String bucketName, Class<T> valueType) {
        return Uni.createFrom().item(() -> connection.keyValue(bucketName));
    }
}
