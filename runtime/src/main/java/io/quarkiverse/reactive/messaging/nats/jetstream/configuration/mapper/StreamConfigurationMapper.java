package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

import java.util.List;

public interface StreamConfigurationMapper {

    List<StreamConfiguration> map(ConnectorConfiguration configuration);

}
