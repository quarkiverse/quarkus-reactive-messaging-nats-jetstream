package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.NativeObjectStore;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectEntry;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.api.ObjectStoreStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

class ObjectStoreImpl implements ObjectStore {
    private final String bucketName;
    private final ClientImpl client;

    public ObjectStoreImpl(String bucketName, ClientImpl client) {
        this.bucketName = bucketName;
        this.client = client;
    }

    @Override
    public @NonNull String bucketName() {
        return bucketName;
    }

    @Override
    public @NonNull Uni<ObjectInfo> put(@NonNull final ObjectMetadata metadata, final byte @NonNull [] data) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try (final var inputStream = new ByteArrayInputStream(data)) {
                        return objectStore.put(ObjectMetadata.of(metadata), inputStream);
                    }
                })))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> put(@NonNull final String objectName, final byte @NonNull [] data) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.put(objectName, data))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectEntry> get(@NonNull final String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try (final var outputStream = new ByteArrayOutputStream()) {
                        final var info = objectStore.get(objectName, outputStream);
                        return ObjectEntry.of(outputStream.toByteArray(), ObjectInfo.of(info));
                    }
                })))
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> info(@NonNull final String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.getInfo(objectName))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> info(@NonNull final String objectName, final boolean includingDeleted) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> objectStore.getInfo(objectName, includingDeleted))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> update(@NonNull String objectName, @NonNull ObjectMetadata metadata) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> objectStore.updateMeta(objectName, ObjectMetadata.of(metadata)))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> delete(@NonNull String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.delete(objectName))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> link(@NonNull String objectName, @NonNull ObjectInfo toInfo) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> objectStore.addLink(objectName, ObjectInfo.of(toInfo)))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> link(@NonNull final String objectName, @NonNull final ObjectStore store) {
        return objectStore(bucketName)
                .chain(objectStore -> nativeObjectStore(store.bucketName()).chain(nativeObjectStore -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> objectStore.addBucketLink(objectName, nativeObjectStore)))))
                .map(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectStoreStatus> seal() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::seal)))
                .map(ObjectStoreStatus::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectStoreStatus> status() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::getStatus)))
                .map(ObjectStoreStatus::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<ObjectInfo> list() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::getList)))
                .onItem().transformToMulti(list -> Multi.createFrom().iterable(list))
                .onItem().transform(ObjectInfo::of)
                .onFailure().transform(ObjectStoreException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<NativeObjectStore> objectStore(
            final String bucketName) {
        return nativeObjectStore(bucketName)
                .map(NativeObjectStore::of);
    }

    private Uni<io.nats.client.ObjectStore> nativeObjectStore(final String bucketName) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(() -> jetStreamManagement.objectStore(bucketName))));
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
