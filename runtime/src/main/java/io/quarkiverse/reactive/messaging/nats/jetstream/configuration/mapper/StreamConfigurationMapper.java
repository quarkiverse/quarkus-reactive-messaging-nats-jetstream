package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

public interface StreamConfigurationMapper {

    List<StreamConfiguration> map(ConnectorConfiguration configuration);

}
