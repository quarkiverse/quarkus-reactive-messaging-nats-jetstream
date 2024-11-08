package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import io.nats.client.api.CompressionOption;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class DefaultStreamConfiguration implements StreamConfiguration {
    private final String name;
    private final String description;
    private final Set<String> subjects;
    private final Integer replicas;
    private final StorageType storageType;
    private final RetentionPolicy retentionPolicy;
    private final CompressionOption compressionOption;
    private final Long maximumConsumers;
    private final Long maximumMessages;
    private final Long maximumMessagesPerSubject;
    private final Long maximumBytes;
    private final Duration maximumAge;
    private final Integer maximumMessageSize;
    private final String templateOwner;
    private final DiscardPolicy discardPolicy;
    private final Duration duplicateWindow;
    private final Boolean sealed;
    private final Boolean allowRollup;
    private final Boolean allowDirect;
    private final Boolean mirrorDirect;
    private final Boolean denyDelete;
    private final Boolean denyPurge;
    private final Boolean discardNewPerSubject;
    private final Long firstSequence;

    @Override
    public String name() {
        return name;
    }

    @Override
    public Optional<String> description() {
        return Optional.ofNullable(description);
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

    @Override
    public CompressionOption compressionOption() {
        return compressionOption;
    }

    @Override
    public Optional<Long> maximumConsumers() {
        return Optional.ofNullable(maximumConsumers);
    }

    @Override
    public Optional<Long> maximumMessages() {
        return Optional.ofNullable(maximumMessages);
    }

    @Override
    public Optional<Long> maximumMessagesPerSubject() {
        return Optional.ofNullable(maximumMessagesPerSubject);
    }

    @Override
    public Optional<Long> maximumBytes() {
        return Optional.ofNullable(maximumBytes);
    }

    @Override
    public Optional<Duration> maximumAge() {
        return Optional.ofNullable(maximumAge);
    }

    @Override
    public Optional<Integer> maximumMessageSize() {
        return Optional.ofNullable(maximumMessageSize);
    }

    @Override
    public Optional<String> templateOwner() {
        return Optional.ofNullable(templateOwner);
    }

    @Override
    public Optional<DiscardPolicy> discardPolicy() {
        return Optional.ofNullable(discardPolicy);
    }

    @Override
    public Optional<Duration> duplicateWindow() {
        return Optional.ofNullable(duplicateWindow);
    }

    @Override
    public Optional<Boolean> allowRollup() {
        return Optional.ofNullable(allowRollup);
    }

    @Override
    public Optional<Boolean> allowDirect() {
        return Optional.ofNullable(allowDirect);
    }

    @Override
    public Optional<Boolean> mirrorDirect() {
        return Optional.ofNullable(mirrorDirect);
    }

    @Override
    public Optional<Boolean> denyDelete() {
        return Optional.ofNullable(denyDelete);
    }

    @Override
    public Optional<Boolean> denyPurge() {
        return Optional.ofNullable(denyPurge);
    }

    @Override
    public Optional<Boolean> discardNewPerSubject() {
        return Optional.ofNullable(discardNewPerSubject);
    }

    @Override
    public Optional<Long> firstSequence() {
        return Optional.ofNullable(firstSequence);
    }
}
