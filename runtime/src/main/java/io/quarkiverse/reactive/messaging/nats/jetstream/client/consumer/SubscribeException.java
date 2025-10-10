package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class SubscribeException extends RuntimeException {
    public SubscribeException(String stream, String consumer, Throwable cause) {
        super(String.format("Unable to subscribe to stream %s with consumer %s", stream, consumer), cause);
    }
}
