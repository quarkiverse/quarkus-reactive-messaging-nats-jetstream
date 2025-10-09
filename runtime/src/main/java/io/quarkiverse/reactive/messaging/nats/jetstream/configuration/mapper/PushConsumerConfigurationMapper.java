package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

import java.util.List;

public interface PushConsumerConfigurationMapper {

    <T> List<PushConsumerConfiguration<T>> map(ConnectorConfiguration configuration);

    <T> List<PushConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration);
}
