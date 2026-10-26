package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

public interface NativeKeyValue extends io.nats.client.KeyValue {

    static NativeKeyValue of(io.nats.client.KeyValue delegate) {
        return new NativeKeyValueDelegate(delegate);
    }
}
