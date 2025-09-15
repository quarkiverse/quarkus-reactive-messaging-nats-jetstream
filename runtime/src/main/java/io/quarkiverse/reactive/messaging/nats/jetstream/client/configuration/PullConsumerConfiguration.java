package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PullConsumerConfiguration<T> {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration<T> consumerConfiguration();

    /**
     * The pull configuration.
     */
    PullConfiguration pullConfiguration();

}
