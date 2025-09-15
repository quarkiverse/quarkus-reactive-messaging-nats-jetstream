package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public interface ErrorListener {

    void onError(Throwable throwable);

}
