package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

public interface PushConsumerConfiguration {

    /**
     * Retrieves the configuration settings of the consumer.
     *
     * @return an instance of {@link ConsumerConfiguration} containing the configuration options
     *         for the consumer, such as durability, acknowledgment wait time, delivery policy,
     *         and many other customizable parameters.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * Retrieves the push configuration settings for the consumer.
     *
     * @return an instance of {@link PushConfiguration} containing options such as ordered delivery,
     *         delivery subject, flow control, idle heartbeats, rate limits, and other push-specific settings.
     */
    PushConfiguration pushConfiguration();

}
