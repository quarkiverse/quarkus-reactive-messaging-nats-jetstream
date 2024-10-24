package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.util.List;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.KeyValueSetupConfiguration;
import io.smallrye.mutiny.Uni;

public interface KeyValueStoreSetup {

    Uni<Void> addOrUpdateKeyValueStores(List<KeyValueSetupConfiguration> keyValueConfigurations);
}
