package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

/**
 * Raised when a {@link JetStreamRequestReply#request(Object)} call does not receive its reply within the configured
 * timeout ({@code reply.timeout} connector attribute, default 5000 ms). The correlation id of the outstanding request is
 * preserved for diagnostics.
 */
public class JetStreamRequestTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String correlationId;

    public JetStreamRequestTimeoutException(String correlationId, long timeoutMillis) {
        super("No reply received within " + timeoutMillis + " ms for correlation id '" + correlationId + "'");
        this.correlationId = correlationId;
    }

    /** @return the correlation id of the request that timed out */
    public String getCorrelationId() {
        return correlationId;
    }
}
