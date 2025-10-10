package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

public interface PushConsumerConfigurationMapper {

    <T> List<PushConsumerConfiguration<T>> map(ConnectorConfiguration configuration);

    <T> List<PushConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration);
}
