package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

public interface PullConsumerConfiguration {

    /**
     * Retrieves the configuration of the consumer.
     *
     * @return an instance of ConsumerConfiguration containing parameters and settings for the consumer.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * Retrieves the pull configuration for managing pull-based message consumption.
     *
     * @return an instance of PullConfiguration containing parameters and settings for handling pull-based consumption.
     */
    PullConfiguration pullConfiguration();
}
