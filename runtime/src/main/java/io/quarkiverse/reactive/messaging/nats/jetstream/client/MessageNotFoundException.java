package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class MessageNotFoundException extends RuntimeException {

    public MessageNotFoundException() {
    }

    public MessageNotFoundException(Throwable cause) {
        super(cause);
    }
}
