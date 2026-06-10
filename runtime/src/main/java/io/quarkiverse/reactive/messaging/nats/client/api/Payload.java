package io.quarkiverse.reactive.messaging.nats.client.api;

public interface Payload<P, T> {

    P data();

    Class<T> type();

}
