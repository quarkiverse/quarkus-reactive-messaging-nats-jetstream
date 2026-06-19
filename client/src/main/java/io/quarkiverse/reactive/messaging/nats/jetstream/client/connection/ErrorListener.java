package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

public interface ErrorListener extends io.nats.client.ErrorListener {

    static ErrorListener of() {
        return new DefaultErrorListener();
    }
}
