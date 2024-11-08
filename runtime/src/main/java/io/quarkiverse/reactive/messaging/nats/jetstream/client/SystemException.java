package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class SystemException extends RuntimeException {

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }
}
