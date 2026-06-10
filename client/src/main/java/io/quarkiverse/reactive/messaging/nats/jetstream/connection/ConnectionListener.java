package io.quarkiverse.reactive.messaging.nats.jetstream.connection;

public interface ConnectionListener extends io.nats.client.ConnectionListener {

    static ConnectionListener of() {
        return new NativeConnectionListener();
    }

}
