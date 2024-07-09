package io.quarkiverse.reactive.messaging.nats.jetstream.client.io;

public class JetStreamReaderException extends RuntimeException {

    public JetStreamReaderException(Throwable cause) {
        super(cause);
    }

    public JetStreamReaderException(String message) {
        super(message);
    }
}
