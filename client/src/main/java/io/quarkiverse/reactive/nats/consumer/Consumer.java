package io.quarkiverse.reactive.nats.consumer;

import io.smallrye.mutiny.Uni;

import java.time.Duration;

/**
 * @see io.nats.client.Consumer
 */
public interface Consumer {

    void setPendingLimits(long maxMessages, long maxBytes);

    Long getPendingMessageLimit();

    Long getPendingByteLimit();

    Long getPendingMessageCount();

    Long getPendingByteCount();

    Long getDeliveredCount();

    Long getDroppedCount();

    void clearDroppedCount();

    Boolean isActive();

    Uni<Boolean> drain(Duration timeout);
}
