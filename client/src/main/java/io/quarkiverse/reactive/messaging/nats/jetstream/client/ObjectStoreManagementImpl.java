package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.ObjectStoreManagementException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.ObjectStoreConfiguration;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ObjectStoreManagementImpl implements ObjectStoreManagement {
    private final ClientImpl client;

    @Override
    public @NonNull Uni<Void> addIfAbsent(@NonNull ObjectStoreConfiguration configuration) {
        return bucketNames()
                .onItem().transformToUni(bucketNames -> {
                    if (!bucketNames.contains(configuration.bucketName())) {
                        return add(configuration);
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })
                .onFailure().transform(ObjectStoreManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    public @NonNull Uni<List<String>> bucketNames() {
        return objectStoreManagement()
                .chain(objectStoreManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(objectStoreManagement::getBucketNames)))
                .onFailure().transform(ObjectStoreManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    private @NonNull Uni<Void> add(@NonNull final ObjectStoreConfiguration configuration) {
        return objectStoreManagement()
                .chain(objectStoreManagement -> Uni.createFrom()
                        .item(Unchecked.supplier(
                                () -> objectStoreManagement.create(ObjectStoreConfiguration.of(configuration)))))
                .chain(keyValueStatus -> Uni.createFrom().voidItem());
    }

    private @NonNull Uni<NativeObjectStoreManagement> objectStoreManagement() {
        return jetStreamManagement()
                .map(Unchecked.function(NativeJetStreamManagement::objectStoreManagement))
                .map(NativeObjectStoreManagementDelegate::new);
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
