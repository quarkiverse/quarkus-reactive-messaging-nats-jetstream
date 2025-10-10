package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.WithDefault;

public interface PushConfiguration {

    /**
     * Flag indicating whether this subscription should be ordered
     */
    @WithDefault("true")
    Boolean ordered();

    /**
     * The subject
     */
    String deliverSubject();

    /**
     * Enables per-subscription flow control using a sliding-window protocol. This protocol relies on the server and
     * client exchanging messages to regulate when and how many messages are pushed to the client. This one-to-one flow
     * control mechanism works in tandem with the one-to-many flow control imposed by MaxAckPending across all
     * subscriptions bound to a consumer
     */
    Optional<Duration> flowControl();

    /**
     * If the idle heartbeat period is set, the server will regularly send a status message to the client
     * (i.e. when the period has elapsed) while there are no new messages to send. This lets the client know that the
     * JetStream service is still up and running, even when there is no activity on the stream. The message status
     * header will have a code of 100. Unlike FlowControl, it will have no reply to address. It may have a description
     * such \"Idle Heartbeat\". Note that this heartbeat mechanism is all handled transparently by supported clients
     * and does not need to be handled by the application
     */
    Optional<Duration> idleHeartbeat();

    /**
     * Used to throttle the delivery of messages to the consumer, in bits per second.
     */
    Optional<Long> rateLimit();

    /**
     * Delivers only the headers of messages in the stream and not the bodies. Additionally adds Nats-Msg-Size header
     * to indicate the size of the removed payload
     */
    Optional<Boolean> headersOnly();

    /**
     * The optional deliver group to join
     */
    Optional<String> deliverGroup();
}
