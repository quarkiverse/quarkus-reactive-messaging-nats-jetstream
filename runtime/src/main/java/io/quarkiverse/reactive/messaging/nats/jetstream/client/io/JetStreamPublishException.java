package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

public class JetStreamPublishException extends RuntimeException {

    public JetStreamPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
