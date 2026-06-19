package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface Consumer extends io.nats.client.Consumer {

    static Consumer of(io.nats.client.Consumer consumer) {
        return new ConsumerDelegate(consumer);
    }

}
