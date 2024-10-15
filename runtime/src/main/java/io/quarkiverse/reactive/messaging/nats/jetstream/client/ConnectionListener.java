package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public interface ConnectionListener extends AutoCloseable {

    void onEvent(ConnectionEvent event, String message);

}
