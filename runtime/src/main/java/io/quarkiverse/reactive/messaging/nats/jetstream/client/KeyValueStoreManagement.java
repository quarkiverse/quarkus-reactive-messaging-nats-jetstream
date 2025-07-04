package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.Map;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueStoreConfiguration;
import io.smallrye.mutiny.Uni;

public interface KeyValueStoreManagement {

    /**
     * Add key values stores. The map key is the name of the bucket
     */
    Uni<Void> addKeyValueStores(Map<String, ? extends KeyValueStoreConfiguration> keyValueConfigurations);

}
