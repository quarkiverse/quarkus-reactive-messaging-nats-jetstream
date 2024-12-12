package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class PullException extends RuntimeException {

    public PullException(Throwable cause) {
        super(cause);
    }

    public PullException(String message, Throwable cause) {
        super(message, cause);
    }
}
