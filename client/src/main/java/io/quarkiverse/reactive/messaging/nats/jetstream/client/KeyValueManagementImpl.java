package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.connection.NativeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueManagement;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueManagementException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration.KeyValueConfiguration;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class KeyValueManagementImpl implements KeyValueManagement {
    private final ClientImpl client;

    @Override
    public @NonNull Uni<Void> addIfAbsent(@NonNull final KeyValueConfiguration configuration) {
        return bucketNames()
                .onItem().transformToUni(bucketNames -> {
                    if (!bucketNames.contains(configuration.bucketName())) {
                        return add(configuration);
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })
                .onFailure().transform(KeyValueManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    @Override
    public @NonNull Uni<List<String>> bucketNames() {
        return keyValueManagement()
                .chain(keyValueManagement -> Uni.createFrom().item(Unchecked.supplier(keyValueManagement::getBucketNames)))
                .onFailure().transform(KeyValueManagementException::new)
                .runSubscriptionOn(executorService())
                .emitOn(this::runOnContext);
    }

    private @NonNull Uni<Void> add(@NonNull final KeyValueConfiguration configuration) {
        return keyValueManagement()
                .chain(keyValueManagement -> Uni.createFrom()
                        .item(Unchecked
                                .supplier(() -> keyValueManagement.create(KeyValueConfiguration.of(configuration)))))
                .chain(keyValueStatus -> Uni.createFrom().voidItem());

    }

    private @NonNull Uni<NativeKeyValueManagement> keyValueManagement() {
        return jetStreamManagement()
                .map(Unchecked.function(NativeJetStreamManagement::keyValueManagement))
                .map(NativeKeyValueManagementDelegate::new);
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
