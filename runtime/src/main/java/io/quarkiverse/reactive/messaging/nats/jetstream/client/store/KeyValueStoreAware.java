package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public interface KeyValueStoreAware {

    <T> T withKeyValueStoreContext(KeyValueStoreContextConsumer<T> consumer);
}
