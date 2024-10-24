package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public class SetupException extends RuntimeException {

    public SetupException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
