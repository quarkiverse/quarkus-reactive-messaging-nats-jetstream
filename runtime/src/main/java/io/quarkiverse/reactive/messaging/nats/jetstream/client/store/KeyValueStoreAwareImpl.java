package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import java.util.List;

import io.nats.client.KeyValue;
import io.nats.client.KeyValueManagement;
import io.nats.client.api.KeyValueConfiguration;
import io.nats.client.api.KeyValueStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.GenericPayload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.GenericSerializedPayload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.Payload;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;

public record KeyValueStoreAwareImpl(ExecutionHolder executionHolder, PayloadMapper payloadMapper,
        KeyValueConfigurationMapper keyValueConfigurationMapper,
        Connection connection) implements KeyValueStoreAware, ContextAware {

    @Override
    public Uni<Void> addKeyValueStoreIfAbsent(final KeyValueStoreConfiguration configuration) {
        return withContext(context -> context.executeBlocking(bucketNames()
                .onItem().transformToUni(bucketNames -> {
                    if (!bucketNames.contains(configuration.name())) {
                        return addKeyValueStore(keyValueConfigurationMapper.map(configuration));
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })
                .onItem().<Void> transform(keyValueStatus -> null)
                .onFailure().transform(ClientException::new)));

    }

    @Override
    public <T> Uni<T> getValue(final String bucketName, final String key, final Class<T> valueType) {
        return withContext(context -> context.executeBlocking(keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new BucketNotFoundException(bucketName))
                .onItem().ifNotNull()
                .transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key))))
                .onItem().ifNotNull().transform(keyValueEntry -> GenericSerializedPayload
                        .<T> builder()
                        .data(keyValueEntry.getValue())
                        .type(valueType)
                        .build())
                .onItem().ifNotNull().transform(payloadMapper::map)
                .onItem().ifNotNull().transform(Payload::data)
                .onFailure().transform(ClientException::new)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Uni<Long> putValue(final String bucketName, final String key, final T value) {
        final var payload = payloadMapper
                .map(GenericPayload.<T, T> builder().data(value).type((Class<T>) value.getClass()).build());
        return withContext(context -> context.executeBlocking(keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new BucketNotFoundException(bucketName))
                .onItem().ifNotNull()
                .transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.put(key, payload.data())))))
                .onFailure().transform(ClientException::new));
    }

    @Override
    public Uni<Void> deleteValue(final String bucketName, final String key) {
        return withContext(context -> context.executeBlocking(keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new BucketNotFoundException(bucketName))
                .onItem().ifNotNull().<Void> transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.delete(key);
                    return null;
                })))
                .onFailure().transform(ClientException::new)));
    }

    private Uni<List<String>> bucketNames() {
        return keyValueManagement()
                .onItem()
                .transformToUni(
                        keyValueManagement -> Uni.createFrom().item(Unchecked.supplier(keyValueManagement::getBucketNames)));
    }

    private Uni<KeyValueManagement> keyValueManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection::keyValueManagement));
    }

    private Uni<KeyValueStatus> addKeyValueStore(final KeyValueConfiguration config) {
        return keyValueManagement()
                .onItem()
                .transformToUni(keyValueManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> keyValueManagement.create(config))));
    }

    private Uni<KeyValue> keyValue(final String bucketName) {
        return Uni.createFrom().item(Unchecked.supplier(() -> connection.keyValue(bucketName)));
    }
}
