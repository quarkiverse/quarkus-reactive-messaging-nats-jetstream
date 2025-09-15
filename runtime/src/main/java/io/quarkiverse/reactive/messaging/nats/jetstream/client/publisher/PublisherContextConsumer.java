package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

@FunctionalInterface
public interface PublisherContextConsumer<T> {

    T accept(PublisherContext context);

}
