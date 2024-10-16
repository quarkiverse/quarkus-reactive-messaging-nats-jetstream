package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class ReaderException extends RuntimeException {

    public ReaderException(Throwable cause) {
        super(cause);
    }

    public ReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReaderException(String message) {
        super(message);
    }
}
