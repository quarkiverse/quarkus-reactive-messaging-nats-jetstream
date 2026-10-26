package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public interface NativeObjectStore extends io.nats.client.ObjectStore {

    static NativeObjectStore of(io.nats.client.ObjectStore delegate) {
        return new NativeObjectStoreDelegate(delegate);
    }
}
