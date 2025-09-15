package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import io.smallrye.mutiny.Uni;
import org.jspecify.annotations.NonNull;

public interface KeyValueStoreContext {

    /**
     * Add key value store.
     */
    @NonNull Uni<Void> addIfAbsent(@NonNull String bucketName, @NonNull KeyValueStoreConfiguration configuration);

    @NonNull<T> Uni<T> get(@NonNull String bucketName, @NonNull String key, @NonNull Class<T> valueType);

    @NonNull <T> Uni<Void> put(@NonNull String bucketName, @NonNull String key, @NonNull T value);

    @NonNull Uni<Void> delete(@NonNull String bucketName, @NonNull String key);

}
