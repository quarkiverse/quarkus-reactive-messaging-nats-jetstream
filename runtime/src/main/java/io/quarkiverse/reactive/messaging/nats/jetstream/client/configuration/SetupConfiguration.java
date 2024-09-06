package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;

public interface SetupConfiguration {
    String stream();

    Set<String> subjects();

    Integer replicas();

    StorageType storageType();

    RetentionPolicy retentionPolicy();

    static List<SetupConfiguration> of(JetStreamBuildConfiguration configuration) {
        return configuration.streams().stream().map(stream -> new DefaultSetupConfiguration(
                stream.name(),
                stream.subjects(),
                configuration.replicas(),
                StorageType.valueOf(configuration.storageType()),
                RetentionPolicy.valueOf(configuration.retentionPolicy())))
                .collect(Collectors.toUnmodifiableList());
    }
}
