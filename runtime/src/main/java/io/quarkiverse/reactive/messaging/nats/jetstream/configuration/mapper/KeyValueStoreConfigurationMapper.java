package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

public interface KeyValueStoreConfigurationMapper {

    List<io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration> map(
            ConnectorConfiguration configuration);

}
