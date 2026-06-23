package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public interface ObjectStore extends io.nats.client.ObjectStore {

    static ObjectStore of(io.nats.client.ObjectStore delegate) {
        return new ObjectStoreDelegate(delegate);
    }
}
