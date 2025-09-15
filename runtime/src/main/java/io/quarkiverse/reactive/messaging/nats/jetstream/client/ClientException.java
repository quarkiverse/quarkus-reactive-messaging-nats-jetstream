package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class ClientException extends RuntimeException {

    public ClientException(Throwable cause) {
        super(cause);
    }

    public ClientException(String message) {
        super(message);
    }
}
