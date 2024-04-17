package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class JetStreamPublishException extends RuntimeException {

    public JetStreamPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
