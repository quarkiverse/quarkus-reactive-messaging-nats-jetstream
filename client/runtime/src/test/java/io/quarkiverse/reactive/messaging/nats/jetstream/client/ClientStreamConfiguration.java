package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.*;

public record ClientStreamConfiguration(@NonNull String name, @NonNull Set<String> subjects) implements StreamConfiguration {

    @Override
    public @NonNull RetentionPolicy retentionPolicy() {
        return RetentionPolicy.WorkQueue;
    }

    @Override
    public @NonNull Compression compression() {
        return Compression.None;
    }

    @Override
    public @NonNull StorageType storageType() {
        return StorageType.File;
    }

    @Override
    public @NonNull DiscardPolicy discardPolicy() {
        return DiscardPolicy.Old;
    }

    @Override
    public @NonNull Optional<String> description() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxConsumers() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxMessages() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxMessagesPerSubject() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxBytes() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> maxAge() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Integer> maximumMessageSize() {
        return Optional.empty();
    }

    @Override
    public int replicas() {
        return 1;
    }

    @Override
    public boolean noAck() {
        return false;
    }

    @Override
    public @NonNull Optional<String> templateOwner() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> duplicateWindow() {
        return Optional.empty();
    }

    @Override
    public @NonNull Set<String> subjects() {
        return subjects;
    }

    @Override
    public @NonNull Optional<Placement> placement() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Republish> republish() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<SubjectTransform> subjectTransform() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<ConsumerLimits> consumerLimits() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Mirror> mirror() {
        return Optional.empty();
    }

    @Override
    public @NonNull List<Source> sources() {
        return List.of();
    }

    @Override
    public boolean sealed() {
        return false;
    }

    @Override
    public boolean allowRollup() {
        return false;
    }

    @Override
    public boolean allowDirect() {
        return false;
    }

    @Override
    public boolean mirrorDirect() {
        return false;
    }

    @Override
    public boolean denyDelete() {
        return false;
    }

    @Override
    public boolean denyPurge() {
        return false;
    }

    @Override
    public boolean discardNewPerSubject() {
        return false;
    }

    @Override
    public @NonNull Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public long firstSequence() {
        return 0;
    }

    @Override
    public @NonNull Optional<Duration> subjectDeleteMarkerTtl() {
        return Optional.empty();
    }

    @Override
    public boolean allowMessageTtl() {
        return false;
    }

    @Override
    public boolean allowMessageSchedules() {
        return false;
    }

    @Override
    public boolean allowMessageCounter() {
        return false;
    }

    @Override
    public boolean allowAtomicPublish() {
        return false;
    }

    @Override
    public boolean allowBatched() {
        return false;
    }

    @Override
    public @NonNull Optional<PersistMode> persistMode() {
        return Optional.empty();
    }
}
