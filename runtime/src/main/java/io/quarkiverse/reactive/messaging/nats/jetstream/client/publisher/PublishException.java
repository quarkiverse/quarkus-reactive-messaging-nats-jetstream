package io.quarkiverse.reactive.messaging.nats.jetstream.client.publisher;

public class PublishException extends RuntimeException {

    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
