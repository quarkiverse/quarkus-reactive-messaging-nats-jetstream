package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

@FunctionalInterface
public interface StreamContextConsumer<T> {
    T accept(StreamContext context);
}
