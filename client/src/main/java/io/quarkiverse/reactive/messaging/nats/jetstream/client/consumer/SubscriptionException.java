package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class SubscriptionException extends RuntimeException {
    public SubscriptionException(Throwable cause) {
        super(cause);
    }
}
