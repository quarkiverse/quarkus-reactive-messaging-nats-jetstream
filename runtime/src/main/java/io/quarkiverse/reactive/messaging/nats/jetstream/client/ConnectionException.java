package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class ConnectionException extends RuntimeException {

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
