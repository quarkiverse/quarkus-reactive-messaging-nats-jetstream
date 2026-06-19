package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

public interface Connection extends io.nats.client.Connection {

    static Connection of(io.nats.client.Connection connection) {
        return new ConnectionDelegate(connection);
    }

}
