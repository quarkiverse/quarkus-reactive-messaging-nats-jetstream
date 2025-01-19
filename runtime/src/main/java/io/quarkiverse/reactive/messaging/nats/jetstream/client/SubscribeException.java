package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class SubscribeException extends RuntimeException {

    public SubscribeException(Throwable cause) {
        super(cause);
    }
}
