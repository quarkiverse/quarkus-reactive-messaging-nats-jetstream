package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public class FetchException extends RuntimeException {

    public FetchException(Throwable cause) {
        super(cause);
    }
}
