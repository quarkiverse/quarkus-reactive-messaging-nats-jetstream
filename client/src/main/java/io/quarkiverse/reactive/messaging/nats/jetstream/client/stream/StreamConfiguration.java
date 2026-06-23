package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record StreamConfiguration(String name,
        String description,
        List<String> subjects,
        RetentionPolicy retentionPolicy,
        CompressionOption compressionOption,
        long maxConsumers,
        long maxMessages,
        long maxMessagesPerSubject,
        long maxBytes,
        Duration maxAge,
        int maximumMessageSize,
        StorageType storageType,
        int replicas,
        boolean noAck,
        String templateOwner,
        DiscardPolicy discardPolicy,
        Duration duplicateWindow,
        Placement placement,
        Republish republish,
        SubjectTransform subjectTransform,
        ConsumerLimits consumerLimits,
        Mirror mirror,
        List<Source> sources,
        boolean sealed,
        boolean allowRollup,
        boolean allowDirect,
        boolean mirrorDirect,
        boolean denyDelete,
        boolean denyPurge,
        boolean discardNewPerSubject,
        Map<String, String> metadata,
        long firstSequence,
        Duration subjectDeleteMarkerTtl,
        boolean allowMessageTtl,
        boolean allowMessageSchedules,
        boolean allowMessageCounter,
        boolean allowAtomicPublish,
        boolean allowBatched,
        PersistMode persistMode) {

}
