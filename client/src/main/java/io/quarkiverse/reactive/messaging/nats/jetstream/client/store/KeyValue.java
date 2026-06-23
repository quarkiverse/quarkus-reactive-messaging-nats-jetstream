package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public interface KeyValue extends io.nats.client.KeyValue {

    static KeyValue of(io.nats.client.KeyValue delegate) {
        return new KeyValueDelegate(delegate);
    }
}
