package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

@FunctionalInterface
public interface KeyValueStoreContextConsumer<T> {

    T accept(KeyValueStoreContext context);

}
