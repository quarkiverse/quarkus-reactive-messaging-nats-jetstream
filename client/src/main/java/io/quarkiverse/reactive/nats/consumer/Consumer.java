package io.quarkiverse.reactive.nats.consumer;

import io.smallrye.mutiny.Uni;

import java.time.Duration;

/**
 * @see io.nats.client.Consumer
 */
public interface Consumer {

    /**
     * @see io.nats.client.Consumer#setPendingLimits(long, long)
     */
    Uni<Void> setPendingLimits(long maxMessages, long maxBytes);

    /**
     * @see io.nats.client.Consumer#getPendingMessageLimit()
     */
    Uni<Long> getPendingMessageLimit();

    /**
     * @see io.nats.client.Consumer#getPendingByteLimit()
     */
    Uni<Long> getPendingByteLimit();

    /**
     * @see io.nats.client.Consumer#getPendingMessageCount()
     */
    Uni<Long> getPendingMessageCount();

    /**
     * @see io.nats.client.Consumer#getPendingByteCount()
     */
    Uni<Long> getPendingByteCount();

    /**
     * @see io.nats.client.Consumer#getDeliveredCount()
     */
    Uni<Long> getDeliveredCount();

    /**
     * @see io.nats.client.Consumer#getDroppedCount()
     */
    Uni<Long> getDroppedCount();

    /**
     * @see io.nats.client.Consumer#clearDroppedCount()
     */
    Uni<Void> clearDroppedCount();

    /**
     * @see io.nats.client.Consumer#isActive()
     */
    Uni<Boolean> isActive();

    /**
     * @see io.nats.client.Consumer#drain(Duration)
     */
    Uni<Boolean> drain(Duration timeout);
}
