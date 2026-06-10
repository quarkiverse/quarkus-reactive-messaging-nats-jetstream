package io.quarkiverse.reactive.nats.jetstream.connection;

public interface ErrorListener extends io.nats.client.ErrorListener {

    static ErrorListener of() {
        return new NativeErrorListener();
    }
}
