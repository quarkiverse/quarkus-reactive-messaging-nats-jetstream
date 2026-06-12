package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

public interface NativeStreamContext extends io.nats.client.StreamContext {

    static NativeStreamContext of(io.nats.client.StreamContext delegate) {
        return new NativeStreamContextDelegate(delegate);
    }
}
