package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface NativeConsumer extends io.nats.client.Consumer {

    static NativeConsumer of(io.nats.client.Consumer consumer) {
        return new NativeConsumerDelegate(consumer);
    }

}
