package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.context.ContextAware;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.KeyValueConfigurationMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper.PayloadMapper;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.providers.connectors.ExecutionHolder;
import org.jspecify.annotations.NonNull;

public record DefaultKeyValueStoreContext(ExecutionHolder executionHolder,
                                          ConnectionFactory connectionFactory,
                                          PayloadMapper payloadMapper,
                                          KeyValueConfigurationMapper keyValueConfigurationMapper) implements KeyValueStoreContext, ConnectionAware, ContextAware {

    @Override
    public @NonNull ConnectionFactory connectionFactory() {
        return connectionFactory;
    }

    @Override
    public @NonNull ExecutionHolder executionHolder() {
        return executionHolder;
    }

    @Override
    public @NonNull Uni<Void> addIfAbsent(@NonNull String bucketName, @NonNull KeyValueStoreConfiguration configuration) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.bucketNames()
                .onItem().transformToUni(bucketNames -> {
                    if (!bucketNames.contains(bucketName)) {
                        return connection.addKeyValueStore(keyValueConfigurationMapper.of(bucketName, configuration));
                    } else {
                        return Uni.createFrom().item(null);
                    }
                })
                .onItem().<Void>transform(keyValueStatus -> null)
                .onFailure().transform(ClientException::new))));

    }

    @Override
    public @NonNull <T> Uni<T> get(@NonNull String bucketName, @NonNull String key, @NonNull Class<T> valueType) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key))))
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().transform(keyValueEntry -> payloadMapper.of(keyValueEntry.getValue(), valueType))
                .onFailure().transform(KeyValueException::new))));
    }

    @Override
    public @NonNull <T> Uni<Void> put(@NonNull String bucketName, @NonNull String key, @NonNull T value) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.put(key, payloadMapper.of(value)))))
                .onItem().<Void>transform(revisionNumber -> null)
                .onFailure().transform(KeyValueException::new))));
    }

    @Override
    public @NonNull Uni<Void> delete(@NonNull String bucketName, @NonNull String key) {
        return withContext(context -> context.executeBlocking(withConnection(connection -> connection.keyValue(bucketName)
                .onItem().ifNull().failWith(() -> new KeyValueNotFoundException(bucketName, key))
                .onItem().ifNotNull().<Void>transformToUni(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.delete(key);
                    return null;
                })))
                .onItem().<Void>transform(revisionNumber -> null)
                .onFailure().transform(KeyValueException::new))));
    }
}
