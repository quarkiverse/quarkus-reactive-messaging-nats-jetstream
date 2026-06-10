package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.client.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.configuration.ConnectorConfiguration;

public interface StreamConfigurationMapper {

    List<? extends StreamConfiguration> map(ConnectorConfiguration configuration);

}
