package io.quarkiverse.reactive.messaging.nats.jetstream.reply;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * A request that has been published and is waiting for its matching reply. Instances are exposed read-only through
 * {@link JetStreamRequestReply#getPendingReplies()}.
 */
public final class PendingReply {
    private final String correlationId;
    private final Instant createdAt;
    private final CompletableFuture<Message<?>> future = new CompletableFuture<>();

    public PendingReply(String correlationId, Instant createdAt) {
        this.correlationId = correlationId;
        this.createdAt = createdAt;
    }

    /** The correlation id this request was published with. */
    public String getCorrelationId() {
        return correlationId;
    }

    /** The instant the request was registered as pending. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    boolean isDone() {
        return future.isDone();
    }

    void complete(Message<?> reply) {
        future.complete(reply);
    }

    void fail(Throwable cause) {
        future.completeExceptionally(cause);
    }

    CompletableFuture<Message<?>> future() {
        return future;
    }
}
