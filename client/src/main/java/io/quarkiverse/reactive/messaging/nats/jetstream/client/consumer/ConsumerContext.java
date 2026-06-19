package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface ConsumerContext extends io.nats.client.ConsumerContext {

    static ConsumerContext of(io.nats.client.ConsumerContext delegate) {
        return new ConsumerContextDelegate(delegate);
    }

}
