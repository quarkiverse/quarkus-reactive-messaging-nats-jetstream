package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class PublishException extends RuntimeException {

    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

    public PublishException(Throwable cause) {
        super(cause);
    }
}
