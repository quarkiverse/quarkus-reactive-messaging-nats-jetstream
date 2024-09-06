package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

public class JetStreamSetupException extends RuntimeException {

    public JetStreamSetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public JetStreamSetupException(Throwable cause) {
        super(cause);
    }
}
