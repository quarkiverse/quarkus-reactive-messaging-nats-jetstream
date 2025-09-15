package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface FetchConsumerConfiguration<T> {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration<T> consumerConfiguration();

    /**
     * The fetch configuration.
     */
    FetchConfiguration fetchConfiguration();

}
