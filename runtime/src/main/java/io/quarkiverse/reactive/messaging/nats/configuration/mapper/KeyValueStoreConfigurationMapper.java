package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.client.store.KeyValueStoreConfiguration;
import io.quarkiverse.reactive.messaging.nats.configuration.ConnectorConfiguration;

public interface KeyValueStoreConfigurationMapper {

    List<KeyValueStoreConfiguration> map(
            ConnectorConfiguration configuration);

}
