package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

import java.util.List;

public interface PullConsumerConfigurationMapper {

    <T> List<PullConsumerConfiguration<T>> map(ConnectorConfiguration configuration);

    <T> List<PullConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration);

}
