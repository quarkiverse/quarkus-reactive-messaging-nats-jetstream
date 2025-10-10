package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

public interface ConsumerConfigurationMapper {

    <T> io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration<T> map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConsumerConfiguration configuration);
}
