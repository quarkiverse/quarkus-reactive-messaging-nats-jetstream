package io.quarkiverse.reactive.messsaging.nats.jetstream.client;

public class ConnectionException extends RuntimeException {

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
