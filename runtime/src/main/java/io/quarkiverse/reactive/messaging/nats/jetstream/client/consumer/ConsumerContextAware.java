package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface ConsumerContextAware {

    <T> T withConsumerContext(ConsumerContextConsumer<T> consumer);
}
