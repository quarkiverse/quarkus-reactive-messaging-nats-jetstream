package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.Optional;

public interface PushConsumerConfiguration {

    /**
     * The consumer configuration.
     */
    ConsumerConfiguration consumerConfiguration();

    /**
     * The push configuration.
     */
    Optional<PushConfiguration> pushConfiguration();

}
