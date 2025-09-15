package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PushConsumerConfiguration<T> {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration<T> consumerConfiguration();

    /**
     * The push configuration.
     */
    PushConfiguration pushConfiguration();

}
