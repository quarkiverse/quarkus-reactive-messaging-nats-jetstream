package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

public class MessageSubscriberException extends RuntimeException {

    public MessageSubscriberException(String message, Throwable cause) {
        super(message, cause);
    }
}
