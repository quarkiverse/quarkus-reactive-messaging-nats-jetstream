package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

public interface PullConsumerConfigurationMapper {

    <T> List<PullConsumerConfiguration<T>> map(ConnectorConfiguration configuration);

    <T> List<PullConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration);

}
