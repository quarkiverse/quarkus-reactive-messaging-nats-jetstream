package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.smallrye.mutiny.Uni;

public interface KeyValueStoreAware {

    /**
     * Add key value store.
     */
    Uni<Void> addKeyValueStoreIfAbsent(KeyValueStoreConfiguration configuration);

    <T> Uni<T> getValue(String bucketName, String key, Class<T> valueType);

    <T> Uni<Long> putValue(String bucketName, String key, T value);

    Uni<Void> deleteValue(String bucketName, String key);

}
