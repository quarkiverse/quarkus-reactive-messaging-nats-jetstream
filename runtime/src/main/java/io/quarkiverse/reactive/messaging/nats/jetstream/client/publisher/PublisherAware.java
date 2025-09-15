package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

public interface PublisherAware {

    <T> T withPublisherContext(PublisherContextConsumer<T> consumer);

}
