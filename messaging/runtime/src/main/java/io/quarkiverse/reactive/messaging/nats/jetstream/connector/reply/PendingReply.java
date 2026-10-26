package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Message;

import lombok.Builder;

/**
 * A request that has been published and is waiting for its matching reply. Instances are exposed read-only through
 * {@link RequestReply#getPendingReplies()}.
 */
@Builder
public record PendingReply<Rep>(String correlationId, Instant createdAt, CompletableFuture<Message<Rep>> future) {

    boolean done() {
        return future.isDone();
    }

    void complete(Message<Rep> reply) {
        future.complete(reply);
    }

    void fail(Throwable cause) {
        future.completeExceptionally(cause);
    }
}
