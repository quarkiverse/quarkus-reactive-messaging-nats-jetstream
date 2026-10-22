package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Builder;
import lombok.NonNull;

@Builder
record StreamConfigurationImpl(@NonNull String name,
        @NonNull RetentionPolicy retentionPolicy,
        @NonNull Compression compression,
        @NonNull StorageType storageType,
        @NonNull DiscardPolicy discardPolicy,
        @NonNull Optional<String> description,
        @NonNull Optional<Long> maxConsumers,
        @NonNull Optional<Long> maxMessages,
        @NonNull Optional<Long> maxMessagesPerSubject,
        @NonNull Optional<Long> maxBytes,
        @NonNull Optional<Duration> maxAge,
        @NonNull Optional<Integer> maximumMessageSize,
        int replicas,
        boolean noAck,
        @NonNull Optional<String> templateOwner,
        @NonNull Optional<Duration> duplicateWindow,
        @NonNull Set<String> subjects,
        @NonNull Optional<Placement> placement,
        @NonNull Optional<Republish> republish,
        @NonNull Optional<SubjectTransform> subjectTransform,
        @NonNull Optional<ConsumerLimits> consumerLimits,
        @NonNull Optional<Mirror> mirror,
        @NonNull List<Source> sources,
        boolean sealed,
        boolean allowRollup,
        boolean allowDirect,
        boolean mirrorDirect,
        boolean denyDelete,
        boolean denyPurge,
        boolean discardNewPerSubject,
        @NonNull Map<String, String> metadata,
        long firstSequence,
        @NonNull Optional<Duration> subjectDeleteMarkerTtl,
        boolean allowMessageTtl,
        boolean allowMessageSchedules,
        boolean allowMessageCounter,
        boolean allowAtomicPublish,
        boolean allowBatched,
        @NonNull Optional<PersistMode> persistMode) implements StreamConfiguration {
}
