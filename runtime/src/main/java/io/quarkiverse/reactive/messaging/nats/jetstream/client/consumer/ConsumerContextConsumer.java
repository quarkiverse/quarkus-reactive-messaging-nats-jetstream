package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

@FunctionalInterface
public interface ConsumerContextConsumer<T> {

    T accept(ConsumerContext consumer);

}
