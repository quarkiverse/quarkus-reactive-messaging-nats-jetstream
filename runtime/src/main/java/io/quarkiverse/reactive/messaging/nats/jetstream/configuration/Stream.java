package io.quarkiverse.reactive.messaging.nats.jetstream.configuration;

import java.util.Map;
import java.util.Optional;

public interface Stream {

    /**
     * Name of stream. If not set then the key of the map is used
     */
    Optional<String> name();

    /**
     * Stream configuration.
     */
    Optional<StreamConfiguration> configuration();

    /**
     * Pull consumer configurations. The map key is the name of the consumer.
     */
    Map<String, PullConsumerConfiguration> pullConsumers();

    /**
     * Push consumer configurations. The map key is the name of the consumer.
     */
    Map<String, PushConsumerConfiguration> pushConsumers();
}
