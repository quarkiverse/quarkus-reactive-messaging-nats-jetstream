package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class AdministrationException extends RuntimeException {

    public AdministrationException(Throwable cause) {
        super(cause);
    }

    public AdministrationException(String message) {
        super(message);
    }

    public AdministrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
