package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.nats.client.api.KeyValueConfiguration;

public interface KeyValueConfigurationMapper {

    KeyValueConfiguration map(final KeyValueStoreConfiguration keyValueStoreConfiguration);
}
