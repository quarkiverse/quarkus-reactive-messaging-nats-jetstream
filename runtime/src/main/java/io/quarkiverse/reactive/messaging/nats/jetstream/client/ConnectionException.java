package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class ConnectionException extends RuntimeException {

    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
