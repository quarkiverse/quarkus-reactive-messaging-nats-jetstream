package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.NativeKeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

class KeyValueImpl implements KeyValue {
    private final String bucketName;
    private final ClientImpl client;

    KeyValueImpl(String bucketName, ClientImpl client) {
        this.bucketName = bucketName;
        this.client = client;
    }

    @Override
    public @NonNull String bucketName() {
        return bucketName;
    }

    @Override
    public @NonNull Uni<KeyValueEntry> get(@NonNull String key) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key))))
                .map(KeyValueEntry::of)
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueEntry> get(@NonNull String key, long revision) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.get(key, revision))))
                .map(KeyValueEntry::of)
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueEntry> put(@NonNull String key, byte[] value) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.put(key, value))))
                .chain(revision -> get(key, revision))
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
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
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
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
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
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
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
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
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<String> keys() {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(keyValue::keys)))
                .onItem().transformToMulti(keys -> Multi.createFrom().iterable(keys))
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueStatus> status() {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(keyValue::getStatus)))
                .map(KeyValueStatus::of)
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    private @NonNull Uni<NativeKeyValue> keyValue(@NonNull final String bucketName) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.keyValue(bucketName))))
                .map(NativeKeyValue::of);
    }

    @SuppressWarnings("resource")
    private @NonNull Uni<NativeJetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(NativeJetStreamManagement::of);
    }

    private @NonNull NativeConnection connection() {
        return client.connection();
    }

    private void runOnContext(@NonNull Runnable action) {
        client.clientContext().runOnContext(action);
    }

    private @NonNull ExecutorService executorService() {
        return client.clientContext().executorService();
    }
}
