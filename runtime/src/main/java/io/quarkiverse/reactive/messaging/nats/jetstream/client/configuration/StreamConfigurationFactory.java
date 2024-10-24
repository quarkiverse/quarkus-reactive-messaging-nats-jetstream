package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.util.Collection;
import java.util.Optional;

public class StreamConfigurationFactory {

    public io.nats.client.api.StreamConfiguration create(StreamConfiguration streamConfiguration) {
        var builder = io.nats.client.api.StreamConfiguration.builder()
                .name(streamConfiguration.name())
                .storageType(streamConfiguration.storageType())
                .retentionPolicy(streamConfiguration.retentionPolicy())
                .replicas(streamConfiguration.replicas())
                .subjects(streamConfiguration.subjects())
                .compressionOption(streamConfiguration.compressionOption());

        builder = streamConfiguration.description().map(builder::description).orElse(builder);
        builder = streamConfiguration.maximumConsumers().map(builder::maxConsumers).orElse(builder);
        builder = streamConfiguration.maximumMessages().map(builder::maxMessages).orElse(builder);
        builder = streamConfiguration.maximumMessagesPerSubject().map(builder::maxMessagesPerSubject).orElse(builder);
        builder = streamConfiguration.maximumBytes().map(builder::maxBytes).orElse(builder);
        builder = streamConfiguration.maximumAge().map(builder::maxAge).orElse(builder);
        builder = streamConfiguration.maximumMessageSize().map(builder::maximumMessageSize).orElse(builder);
        builder = streamConfiguration.templateOwner().map(builder::templateOwner).orElse(builder);
        builder = streamConfiguration.discardPolicy().map(builder::discardPolicy).orElse(builder);
        builder = streamConfiguration.duplicateWindow().map(builder::duplicateWindow).orElse(builder);
        builder = streamConfiguration.allowRollup().map(builder::allowRollup).orElse(builder);
        builder = streamConfiguration.allowDirect().map(builder::allowDirect).orElse(builder);
        builder = streamConfiguration.mirrorDirect().map(builder::mirrorDirect).orElse(builder);
        builder = streamConfiguration.denyDelete().map(builder::denyDelete).orElse(builder);
        builder = streamConfiguration.denyPurge().map(builder::denyPurge).orElse(builder);
        builder = streamConfiguration.discardNewPerSubject().map(builder::discardNewPerSubject).orElse(builder);
        builder = streamConfiguration.firstSequence().map(builder::firstSequence).orElse(builder);

        return builder.build();
    }

    public Optional<io.nats.client.api.StreamConfiguration> create(io.nats.client.api.StreamConfiguration current,
            StreamConfiguration streamConfiguration) {
        var updated = false;
        var builder = io.nats.client.api.StreamConfiguration.builder();
        builder.name(current.getName());
        final var description = compare(current.getDescription(), streamConfiguration.description().orElse(null));
        if (description.isPresent()) {
            builder = builder.description(description.get());
            updated = true;
        }
        final var storageType = compare(current.getStorageType(), streamConfiguration.storageType());
        if (storageType.isPresent()) {
            builder = builder.storageType(storageType.get());
            updated = true;
        }
        final var retentionPolicy = compare(current.getRetentionPolicy(), streamConfiguration.retentionPolicy());
        if (retentionPolicy.isPresent()) {
            builder = builder.retentionPolicy(retentionPolicy.get());
            updated = true;
        }
        final var replicas = compare(current.getReplicas(), streamConfiguration.replicas());
        if (replicas.isPresent()) {
            builder = builder.replicas(replicas.get());
            updated = true;
        }
        final var subjects = compare(current.getSubjects(), streamConfiguration.subjects());
        if (subjects.isPresent()) {
            builder = builder.subjects(subjects.get());
            updated = true;
        }
        final var compressionOption = compare(current.getCompressionOption(), streamConfiguration.compressionOption());
        if (compressionOption.isPresent()) {
            builder = builder.compressionOption(compressionOption.get());
            updated = true;
        }
        final var maximumConsumers = compare(current.getMaxConsumers(), streamConfiguration.maximumConsumers().orElse(null));
        if (maximumConsumers.isPresent()) {
            builder = builder.maxConsumers(maximumConsumers.get());
            updated = true;
        }
        final var maximumMessages = compare(current.getMaxMsgs(), streamConfiguration.maximumMessages().orElse(null));
        if (maximumMessages.isPresent()) {
            builder = builder.maxMessages(maximumMessages.get());
            updated = true;
        }
        final var maximumMessagesPerSubject = compare(current.getMaxMsgsPerSubject(),
                streamConfiguration.maximumMessagesPerSubject().orElse(null));
        if (maximumMessagesPerSubject.isPresent()) {
            builder = builder.maxMessagesPerSubject(maximumMessagesPerSubject.get());
            updated = true;
        }
        final var maximumBytes = compare(current.getMaxBytes(), streamConfiguration.maximumBytes().orElse(null));
        if (maximumBytes.isPresent()) {
            builder = builder.maxBytes(maximumBytes.get());
            updated = true;
        }
        final var maximumAge = compare(current.getMaxAge(), streamConfiguration.maximumAge().orElse(null));
        if (maximumAge.isPresent()) {
            builder = builder.maxAge(maximumAge.get());
            updated = true;
        }
        final var maximumMessageSize = compare(current.getMaximumMessageSize(),
                streamConfiguration.maximumMessageSize().orElse(null));
        if (maximumMessageSize.isPresent()) {
            builder = builder.maximumMessageSize(maximumMessageSize.get());
            updated = true;
        }
        final var templateOwner = compare(current.getTemplateOwner(), streamConfiguration.templateOwner().orElse(null));
        if (templateOwner.isPresent()) {
            builder = builder.templateOwner(templateOwner.get());
            updated = true;
        }
        final var discardPolicy = compare(current.getDiscardPolicy(), streamConfiguration.discardPolicy().orElse(null));
        if (discardPolicy.isPresent()) {
            builder = builder.discardPolicy(discardPolicy.get());
            updated = true;
        }
        final var duplicateWindow = compare(current.getDuplicateWindow(), streamConfiguration.duplicateWindow().orElse(null));
        if (duplicateWindow.isPresent()) {
            builder = builder.duplicateWindow(duplicateWindow.get());
            updated = true;
        }
        final var allowRollup = compare(current.getAllowRollup(), streamConfiguration.allowRollup().orElse(null));
        if (allowRollup.isPresent()) {
            builder = builder.allowRollup(allowRollup.get());
            updated = true;
        }
        final var allowDirect = compare(current.getAllowDirect(), streamConfiguration.allowDirect().orElse(null));
        if (allowDirect.isPresent()) {
            builder = builder.allowDirect(allowDirect.get());
            updated = true;
        }
        final var mirrorDirect = compare(current.getMirrorDirect(), streamConfiguration.mirrorDirect().orElse(null));
        if (mirrorDirect.isPresent()) {
            builder = builder.mirrorDirect(mirrorDirect.get());
            updated = true;
        }
        final var denyDelete = compare(current.getDenyDelete(), streamConfiguration.denyDelete().orElse(null));
        if (denyDelete.isPresent()) {
            builder = builder.denyDelete(denyDelete.get());
            updated = true;
        }
        final var denyPurge = compare(current.getDenyPurge(), streamConfiguration.denyPurge().orElse(null));
        if (denyPurge.isPresent()) {
            builder = builder.denyPurge(denyPurge.get());
            updated = true;
        }
        final var discardNewPerSubject = compare(current.isDiscardNewPerSubject(),
                streamConfiguration.discardNewPerSubject().orElse(null));
        if (discardNewPerSubject.isPresent()) {
            builder = builder.discardNewPerSubject(discardNewPerSubject.get());
            updated = true;
        }
        final var firstSequence = compare(current.getFirstSequence(), streamConfiguration.firstSequence().orElse(null));
        if (firstSequence.isPresent()) {
            builder = builder.firstSequence(firstSequence.get());
            updated = true;
        }
        if (updated) {
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    private <T> Optional<T> compare(T currentValue, T newValue) {
        if (currentValue != null && newValue != null && !currentValue.equals(newValue)) {
            return Optional.of(newValue);
        } else if (currentValue == null && newValue != null) {
            return Optional.of(newValue);
        } else {
            return Optional.empty();
        }
    }

    private <T> Optional<Collection<T>> compare(Collection<T> currentValue, Collection<T> newValue) {
        if (currentValue != null && newValue != null && !currentValue.containsAll(newValue)) {
            return Optional.of(newValue);
        } else if (currentValue == null && newValue != null) {
            return Optional.of(newValue);
        } else {
            return Optional.empty();
        }
    }
}
