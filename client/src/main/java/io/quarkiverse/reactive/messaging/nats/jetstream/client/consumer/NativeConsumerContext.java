package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface NativeConsumerContext extends io.nats.client.ConsumerContext {

    static NativeConsumerContext of(io.nats.client.ConsumerContext delegate) {
        return new ConsumerContextDelegate(delegate);
    }

}
