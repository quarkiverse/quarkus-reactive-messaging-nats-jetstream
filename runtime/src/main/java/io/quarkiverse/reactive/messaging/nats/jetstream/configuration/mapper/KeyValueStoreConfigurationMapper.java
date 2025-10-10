package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

import java.util.List;

public interface KeyValueStoreConfigurationMapper {

    List<io.quarkiverse.reactive.messaging.nats.jetstream.client.store.KeyValueStoreConfiguration> map(
            ConnectorConfiguration configuration);



}
