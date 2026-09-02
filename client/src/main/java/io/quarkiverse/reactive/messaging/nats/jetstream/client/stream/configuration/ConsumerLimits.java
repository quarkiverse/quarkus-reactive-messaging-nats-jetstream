package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.Duration;
import java.util.Optional;

import lombok.NonNull;

/**
 * ConsumerLimits
 */
public interface ConsumerLimits {

    /**
     * Maximum value for max_ack_pending for consumers of this stream. Acts as a default when consumers do not set this value.
     *
     * @return maximum ack pending limit
     */
    long maxAckPending();

    /**
     * Maximum value for inactive_threshold for consumers of this stream. Acts as a default when consumers do not set this
     * value.
     *
     * @return the inactive threshold limit
     */
    @NonNull
    Optional<Duration> inactiveThreshold();
}
