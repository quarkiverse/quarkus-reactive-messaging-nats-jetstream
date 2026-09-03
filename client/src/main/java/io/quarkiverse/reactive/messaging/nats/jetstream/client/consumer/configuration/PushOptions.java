package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.smallrye.config.WithDefault;

/**
 * Options for push-based consumption.
 */
public interface PushOptions {

    /**
     * The deliver subject
     *
     * @return the deliver subject
     */
    @NonNull
    String deliverSubject();

    /**
     * Enables per-subscription flow control using a sliding-window protocol. This protocol relies on the server and
     * client exchanging messages to regulate when and how many messages are pushed to the client. This one-to-one flow
     * control mechanism works in tandem with the one-to-many flow control imposed by MaxAckPending across all
     * subscriptions bound to a consumer
     *
     * @return whether flow control is enabled
     */
    @WithDefault("false")
    boolean flowControl();

    /**
     * If the idle heartbeat period is set, the server will regularly send a status message to the client
     * (i.e. when the period has elapsed) while there are no new messages to send. This lets the client know that the
     * JetStream service is still up and running, even when there is no activity on the stream. The message status
     * header will have a code of 100. Unlike FlowControl, it will have no reply to address. It may have a description
     * such \"Idle Heartbeat\". Note that this heartbeat mechanism is all handled transparently by supported clients
     * and does not need to be handled by the application
     *
     * @return the idle heartbeat period
     */
    @NonNull
    Optional<Duration> idleHeartbeat();

    /**
     * Used to throttle the delivery of messages to the consumer, in bits per second.
     *
     * @return the rate limit
     */
    @NonNull
    Optional<Long> rateLimit();

    /**
     * The optional deliver group to join
     *
     * @return the deliver group
     */
    @NonNull
    Optional<String> deliverGroup();

}
