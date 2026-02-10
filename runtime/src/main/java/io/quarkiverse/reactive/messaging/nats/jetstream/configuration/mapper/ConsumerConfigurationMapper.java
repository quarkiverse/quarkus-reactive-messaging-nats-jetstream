package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

public interface ConsumerConfigurationMapper {

    io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConsumerConfiguration configuration);
}
