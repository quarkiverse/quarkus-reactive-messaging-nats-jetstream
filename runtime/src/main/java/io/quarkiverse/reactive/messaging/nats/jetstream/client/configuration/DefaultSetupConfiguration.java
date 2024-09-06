package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.Set;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;

public class DefaultSetupConfiguration implements SetupConfiguration {
    private final String stream;
    private final Set<String> subjects;
    private final Integer replicas;
    private final StorageType storageType;
    private final RetentionPolicy retentionPolicy;

    public DefaultSetupConfiguration(String stream,
            Set<String> subjects,
            Integer replicas,
            StorageType storageType,
            RetentionPolicy retentionPolicy) {
        this.stream = stream;
        this.subjects = subjects;
        this.replicas = replicas;
        this.storageType = storageType;
        this.retentionPolicy = retentionPolicy;
    }

    @Override
    public String stream() {
        return stream;
    }

    @Override
    public Set<String> subjects() {
        return subjects;
    }

    @Override
    public Integer replicas() {
        return replicas;
    }

    @Override
    public StorageType storageType() {
        return storageType;
    }

    @Override
    public RetentionPolicy retentionPolicy() {
        return retentionPolicy;
    }
}
