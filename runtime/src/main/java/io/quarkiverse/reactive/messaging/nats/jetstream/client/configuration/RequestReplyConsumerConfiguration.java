package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;

public interface RequestReplyConsumerConfiguration {

    /**
     * The name of the consumer.
     */
    String name();

    /**
     * The name of the stream.
     */
    String stream();

    /**
     * The subject to publish request to.
     */
    String requestSubject();

    /**
     * The configuration for the consumer.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * The timeout for the reply.
     */
    Duration timeout();

}
