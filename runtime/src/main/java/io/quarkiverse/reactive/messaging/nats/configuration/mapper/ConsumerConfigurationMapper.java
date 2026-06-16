package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerConfiguration;

public interface ConsumerConfigurationMapper {

    ConsumerConfiguration map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.configuration.ConsumerConfiguration configuration);
}
