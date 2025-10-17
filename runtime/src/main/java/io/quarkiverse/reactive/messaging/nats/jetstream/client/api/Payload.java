package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

public interface Payload<P, T> {

    P data();

    Class<T> type();

}
