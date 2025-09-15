package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

public interface StreamContextAware {

    <T> T withStreamContext(StreamContextConsumer<T> consumer);
}
