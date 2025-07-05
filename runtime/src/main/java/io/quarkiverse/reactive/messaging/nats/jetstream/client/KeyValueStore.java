package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.smallrye.mutiny.Uni;

public interface KeyValueStore {

    <T> Uni<T> get(String key, Class<T> valueType);

    <T> Uni<Void> put(String key, T value);

    Uni<Void> delete(String key);

}
