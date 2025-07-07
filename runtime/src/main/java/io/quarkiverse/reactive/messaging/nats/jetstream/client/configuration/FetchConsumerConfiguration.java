package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface FetchConsumerConfiguration {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * The fetch configuration.
     */
    FetchConfiguration fetchConfiguration();

}
