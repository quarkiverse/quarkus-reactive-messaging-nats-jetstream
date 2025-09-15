package io.quarkiverse.reactive.messaging.nats.jetstream.client.connection;

public class ConnectionException extends RuntimeException {

    public ConnectionException(Throwable cause) {
        super(cause);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
