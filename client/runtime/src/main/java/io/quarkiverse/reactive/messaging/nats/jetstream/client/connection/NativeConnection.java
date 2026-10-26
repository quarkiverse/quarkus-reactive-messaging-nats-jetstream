package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

public interface NativeConnection extends io.nats.client.Connection {

    static NativeConnection of(io.nats.client.Connection connection) {
        return new NativeConnectionDelegate(connection);
    }

}
