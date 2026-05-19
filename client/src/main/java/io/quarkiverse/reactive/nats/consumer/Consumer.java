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
    void setPendingLimits(long maxMessages, long maxBytes);

    /**
     * @see io.nats.client.Consumer#getPendingMessageLimit()
     */
    Long getPendingMessageLimit();

    /**
     * @see io.nats.client.Consumer#getPendingByteLimit()
     */
    Long getPendingByteLimit();

    /**
     * @see io.nats.client.Consumer#getPendingMessageCount()
     */
    Long getPendingMessageCount();

    /**
     * @see io.nats.client.Consumer#getPendingByteCount()
     */
    Long getPendingByteCount();

    /**
     * @see io.nats.client.Consumer#getDeliveredCount()
     */
    Long getDeliveredCount();

    /**
     * @see io.nats.client.Consumer#getDroppedCount()
     */
    Long getDroppedCount();

    /**
     * @see io.nats.client.Consumer#clearDroppedCount()
     */
    void clearDroppedCount();

    /**
     * @see io.nats.client.Consumer#isActive()
     */
    Boolean isActive();

    /**
     * @see io.nats.client.Consumer#drain(Duration)
     */
    Uni<Boolean> drain(Duration timeout);
}
