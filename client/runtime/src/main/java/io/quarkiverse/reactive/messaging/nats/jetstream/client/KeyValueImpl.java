package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.nats.client.MessageTtl;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.NativeKeyValue;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.KeyValueStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("ReactiveStreamsUnusedPublisher")
@RequiredArgsConstructor
class KeyValueImpl implements KeyValue {
    private final String bucketName;
    private final NativeConnection connection;
    private final Context context;

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
    public @NonNull Uni<KeyValueEntry> create(@NonNull String key, byte[] value) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> keyValue.create(key, value))))
                .chain(revision -> get(key, revision))
                .onFailure().transform(KeyValueException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<KeyValueEntry> create(@NonNull String key, byte[] value, @NonNull Duration ttl) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> keyValue.create(key, value, messageTtl(ttl)))))
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
    public @NonNull Uni<Void> purge(@NonNull String key, @NonNull Duration ttl) {
        return keyValue(bucketName)
                .chain(keyValue -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    keyValue.purge(key, messageTtl(ttl));
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

    /**
     * Converts a {@link Duration} to a {@link MessageTtl}, rounding up to whole seconds since the server
     * truncates TTLs to seconds.
     */
    static @NonNull MessageTtl messageTtl(@NonNull Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("TTL must be positive: " + ttl);
        }
        final long seconds = ttl.getSeconds() + (ttl.getNano() > 0 ? 1 : 0);
        if (seconds > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("TTL is too large: " + ttl);
        }
        return MessageTtl.seconds((int) seconds);
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
        return connection;
    }

    private void runOnContext(@NonNull Runnable action) {
        context.runOnContext(action);
    }

    private @NonNull ExecutorService executorService() {
        return context.executorService();
    }
}
