package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPullConsumerConfiguration;

public interface RequestReplyConfiguration<T> extends JetStreamPullConsumerConfiguration {

    StorageType storageType();

    RetentionPolicy retentionPolicy();

    Optional<Class<T>> payloadType();

    boolean traceEnabled();

    Duration connectionTimeout();
}
