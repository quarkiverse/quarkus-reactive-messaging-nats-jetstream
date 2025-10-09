package io.quarkiverse.reactive.messaging.nats.jetstream.client.store;

import io.nats.client.api.StorageType;
import lombok.Builder;

import java.time.Duration;
import java.util.Optional;

@Builder
public record KeyValueStoreConfigurationImpl(String name,
                                             Optional<String> description,
                                             StorageType storageType,
                                             Optional<Long> maxBucketSize,
                                             Optional<Integer> maxHistoryPerKey,
                                             Optional<Integer> maxValueSize,
                                             Optional<Duration> ttl,
                                             Optional<Integer> replicas,
                                             Optional<Boolean> compressed) implements KeyValueStoreConfiguration {
 }
