package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueEntryMapper;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStatus;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStatusMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;

class VertxKeyValue implements KeyValue {
    private final String bucketName;
    private final VertxClient client;
    private final KeyValueEntryMapper keyValueEntryMapper;
    private final KeyValueStatusMapper keyValueStatusMapper;

    VertxKeyValue(String bucketName, VertxClient client) {
        this.bucketName = bucketName;
        this.client = client;
        this.keyValueEntryMapper = Mappers.getMapper(KeyValueEntryMapper.class);
        this.keyValueStatusMapper = Mappers.getMapper(KeyValueStatusMapper.class);
    }

    @Override
    public @NonNull String bucketName() {
        return bucketName;
    }

    @Override
    public @NonNull Uni<KeyValueEntry> get(@NonNull String key) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key))))
                .map(keyValueEntryMapper::map)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueEntry> get(@NonNull String key, long revision) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key, revision))))
                .map(keyValueEntryMapper::map)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueEntry> put(@NonNull String key, byte[] value) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.put(key, value))))
                .chain(revision -> get(key, revision))
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> delete(@NonNull String key) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.delete(key);
                    return keyValue;
                })))
                .chain(keyValue -> Uni.createFrom().voidItem())
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> delete(@NonNull String key, long expectedRevision) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.delete(key, expectedRevision);
                    return keyValue;
                })))
                .chain(keyValue -> Uni.createFrom().voidItem())
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> purge(@NonNull String key) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.purge(key);
                    return keyValue;
                })))
                .chain(keyValue -> Uni.createFrom().voidItem())
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<Void> purge(@NonNull String key, long expectedRevision) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.purge(key, expectedRevision);
                    return keyValue;
                })))
                .chain(keyValue -> Uni.createFrom().voidItem())
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<String> keys() {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(keyValue::keys)))
                .onItem().transformToMulti(keys -> Multi.createFrom().iterable(keys))
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueStatus> status() {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(keyValue::getStatus)))
                .map(keyValueStatusMapper::map)
                .runSubscriptionOn(client.executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue> keyValue(final String bucketName) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.keyValue(bucketName))))
                .map(io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue::of);
    }

    private Connection connection() {
        return client.connection();
    }

    private Context context() {
        return client.context();
    }

    private void runOnContext(Runnable action) {
        context().runOnContext(action);
    }

    @SuppressWarnings("resource")
    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(JetStreamManagement::of);
    }
}
