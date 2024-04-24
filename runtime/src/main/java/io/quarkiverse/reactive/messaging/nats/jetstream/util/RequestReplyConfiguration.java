package io.quarkiverse.reactive.messaging.nats.jetstream.util;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;

public interface RequestReplyConfiguration<T> {

    String stream();

    String subject();

    Integer replicas();

    StorageType storageType();

    RetentionPolicy retentionPolicy();

    Class<T> payloadType();

    boolean traceEnabled();

    Duration pollTimeout();

    Optional<Integer> maxDeliver();

    Optional<String> durable();
}
