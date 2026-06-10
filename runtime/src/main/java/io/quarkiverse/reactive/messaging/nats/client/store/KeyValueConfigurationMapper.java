package io.quarkiverse.reactive.messaging.nats.client.store;

import io.nats.client.api.KeyValueConfiguration;

public interface KeyValueConfigurationMapper {

    KeyValueConfiguration map(final KeyValueStoreConfiguration keyValueStoreConfiguration);
}
