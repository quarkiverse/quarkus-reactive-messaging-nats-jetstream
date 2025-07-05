package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.api.CompressionOption;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import lombok.Builder;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Builder
public record DefaultStreamConfiguration(Optional<String> description,
        Optional<Set<String>> subjects,
        Integer replicas,
        StorageType storageType,
        RetentionPolicy retentionPolicy,
        CompressionOption compressionOption,
        Optional<Long> maximumConsumers,
        Optional<Long> maximumMessages,
        Optional<Long> maximumMessagesPerSubject,
        Optional<Long> maximumBytes,
        Optional<Duration> maximumAge,
        Optional<Integer> maximumMessageSize,
        Optional<String> templateOwner,
        Optional<DiscardPolicy> discardPolicy,
        Optional<Duration> duplicateWindow,
        Optional<Boolean> allowRollup,
        Optional<Boolean> allowDirect,
        Optional<Boolean> mirrorDirect,
        Optional<Boolean> denyDelete,
        Optional<Boolean> denyPurge,
        Optional<Boolean> discardNewPerSubject,
        Optional<Long> firstSequence,
        Map<String, ConsumerConfiguration> consumers) implements StreamConfiguration {
}
