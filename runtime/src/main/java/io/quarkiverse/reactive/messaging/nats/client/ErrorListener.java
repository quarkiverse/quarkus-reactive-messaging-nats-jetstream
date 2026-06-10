package io.quarkiverse.reactive.messaging.nats.client;

public interface ErrorListener {

    void onError(Throwable throwable);

}
