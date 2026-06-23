package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.mutiny.core.Context;
import org.jspecify.annotations.NonNull;
import org.mapstruct.factory.Mappers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

class VertxObjectStore implements ObjectStore {
    private final String bucketName;
    private final VertxClient client;
    private final ObjectMetadataMapper objectMetadataMapper;
    private final ObjectInfoMapper objectInfoMapper;
    private final ObjectStoreStatusMapper objectStoreStatusMapper;

    public VertxObjectStore(String bucketName, VertxClient client) {
        this.bucketName = bucketName;
        this.client = client;
        this.objectMetadataMapper = Mappers.getMapper(ObjectMetadataMapper.class);
        this.objectInfoMapper = Mappers.getMapper(ObjectInfoMapper.class);
        this.objectStoreStatusMapper = Mappers.getMapper(ObjectStoreStatusMapper.class);
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
                        return objectStore.put(objectMetadataMapper.map(metadata), inputStream);
                    }
                })))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> put(@NonNull final String objectName, final byte @NonNull [] data) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.put(objectName, data))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectEntry> get(@NonNull final String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> {
                    try (final var outputStream = new ByteArrayOutputStream()) {
                        final var info = objectStore.get(objectName, outputStream);
                        return ObjectEntry.builder().info(objectInfoMapper.map(info)).data(outputStream.toByteArray()).build();
                    }
                })))
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> info(@NonNull final String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.getInfo(objectName))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> info(@NonNull final String objectName, final boolean includingDeleted) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.getInfo(objectName, includingDeleted))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> update(@NonNull String objectName, @NonNull ObjectMetadata metadata) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.updateMeta(objectName, objectMetadataMapper.map(metadata)))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> delete(@NonNull String objectName) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.delete(objectName))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> link(@NonNull String objectName, @NonNull ObjectInfo toInfo) {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.addLink(objectName, objectInfoMapper.map(toInfo)))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectInfo> link(@NonNull final String objectName, @NonNull final ObjectStore store) {
        return objectStore(bucketName)
                .chain(objectStore -> nativeObjectStore(store.bucketName()).chain(nativeObjectStore -> Uni.createFrom().item(Unchecked.supplier(() -> objectStore.addBucketLink(objectName, nativeObjectStore)))))
                .map(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectStoreStatus> seal() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::seal)))
                .map(objectStoreStatusMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<ObjectStoreStatus> status() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::getStatus)))
                .map(objectStoreStatusMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Multi<ObjectInfo> list() {
        return objectStore(bucketName)
                .chain(objectStore -> Uni.createFrom().item(Unchecked.supplier(objectStore::getList)))
                .onItem().transformToMulti(list -> Multi.createFrom().iterable(list))
                .onItem().transform(objectInfoMapper::map)
                .runSubscriptionOn(configuration().executorService())
                .emitOn(this::runOnContext);
    }

    private Uni<io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore> objectStore(final String bucketName) {
        return nativeObjectStore(bucketName)
                .map(io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStore::of);
    }

    private Uni<io.nats.client.ObjectStore> nativeObjectStore(final String bucketName) {
        return jetStreamManagement()
                .chain(jetStreamManagement -> Uni.createFrom().item(Unchecked.supplier(() -> jetStreamManagement.objectStore(bucketName))));
    }

    private ClientConfiguration configuration() {
        return client.configuration();
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

    private Uni<JetStreamManagement> jetStreamManagement() {
        return Uni.createFrom().item(Unchecked.supplier(connection()::jetStreamManagement))
                .map(JetStreamManagement::of);
    }
}
