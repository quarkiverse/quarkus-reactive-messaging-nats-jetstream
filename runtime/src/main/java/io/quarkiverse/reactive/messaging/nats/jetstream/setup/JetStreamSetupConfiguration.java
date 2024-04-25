package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamBuildConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;

public interface JetStreamSetupConfiguration {
    String stream();

    Set<String> subjects();

    Integer replicas();

    StorageType storageType();

    RetentionPolicy retentionPolicy();

    static List<JetStreamSetupConfiguration> of(JetStreamBuildConfiguration configuration) {
        return configuration.streams().stream().map(stream -> new DefaultJetStreamSetupConfiguration(
                stream.name(),
                stream.subjects(),
                configuration.replicas(),
                StorageType.valueOf(configuration.storageType()),
                RetentionPolicy.valueOf(configuration.retentionPolicy())))
                .collect(Collectors.toUnmodifiableList());
    }

    static <T> JetStreamSetupConfiguration of(RequestReplyConfiguration<T> configuration) {
        return new DefaultJetStreamSetupConfiguration(configuration.stream(), Set.of(configuration.subject()),
                configuration.replicas().orElse(1), configuration.storageType(), configuration.retentionPolicy());
    }
}
