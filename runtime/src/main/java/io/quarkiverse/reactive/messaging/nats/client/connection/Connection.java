package io.quarkiverse.reactive.messaging.nats.client.connection;

public interface Connection extends io.nats.client.Connection {

    boolean isConnected();

}
