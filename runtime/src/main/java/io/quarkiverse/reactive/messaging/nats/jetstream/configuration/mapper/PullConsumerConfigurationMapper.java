package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

public interface PullConsumerConfigurationMapper {

    List<PullConsumerConfiguration> map(ConnectorConfiguration configuration);

    List<PullConsumerConfiguration> map(String stream, ConnectorConfiguration configuration);

}
