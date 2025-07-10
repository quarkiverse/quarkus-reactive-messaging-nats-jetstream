package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PullConsumerConfiguration {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * The pull configuration.
     */
    PullConfiguration pullConfiguration();

}
