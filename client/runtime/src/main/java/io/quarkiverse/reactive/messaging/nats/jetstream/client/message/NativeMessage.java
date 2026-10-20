package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

public interface NativeMessage extends io.nats.client.Message {

    static NativeMessage of(io.nats.client.Message message) {
        return new NativeMessageDelegate(message);
    }

}
