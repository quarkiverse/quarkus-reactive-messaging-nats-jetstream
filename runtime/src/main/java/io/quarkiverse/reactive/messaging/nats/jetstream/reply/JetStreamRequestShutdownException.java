package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

/**
 * Raised when a {@link JetStreamRequestReply} instance is closed (application shutdown) while requests are still
 * outstanding, or when a new request is made after the instance has been closed.
 */
public class JetStreamRequestShutdownException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public JetStreamRequestShutdownException(String channelName) {
        super("JetStream request/reply emitter for channel '" + channelName + "' was shut down");
    }
}
