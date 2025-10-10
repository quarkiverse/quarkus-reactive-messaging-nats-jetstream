package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.util.HashSet;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StreamConfigurationMapperImpl implements StreamConfigurationMapper {

    @Override
    public io.nats.client.api.StreamConfiguration map(StreamConfiguration streamConfiguration) {
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
        builder = streamConfiguration.maximumMessagesPerSubject().map(builder::maxMessagesPerSubject)
                .orElse(builder);
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

    @Override
    public StreamConfiguration map(io.nats.client.api.StreamConfiguration configuration) {
        return StreamConfigurationImpl.builder()
                .description(Optional.ofNullable(configuration.getDescription()))
                .subjects(new HashSet<>(configuration.getSubjects()))
                .replicas(configuration.getReplicas())
                .storageType(configuration.getStorageType())
                .retentionPolicy(configuration.getRetentionPolicy())
                .compressionOption(configuration.getCompressionOption())
                .maximumConsumers(Optional.of(configuration.getMaxConsumers()))
                .maximumMessages(Optional.of((long) configuration.getMaximumMessageSize()))
                .maximumMessagesPerSubject(Optional.of(configuration.getMaxMsgsPerSubject()))
                .maximumBytes(Optional.of(configuration.getMaxBytes()))
                .maximumAge(Optional.of(configuration.getMaxAge()))
                .maximumMessageSize(Optional.of(configuration.getMaximumMessageSize()))
                .templateOwner(Optional.ofNullable(configuration.getTemplateOwner()))
                .discardPolicy(Optional.ofNullable(configuration.getDiscardPolicy()))
                .duplicateWindow(Optional.ofNullable(configuration.getDuplicateWindow()))
                .allowRollup(Optional.of(configuration.getAllowRollup()))
                .allowDirect(Optional.of(configuration.getAllowDirect()))
                .mirrorDirect(Optional.of(configuration.getMirrorDirect()))
                .denyDelete(Optional.of(configuration.getDenyDelete()))
                .discardNewPerSubject(Optional.of(configuration.isDiscardNewPerSubject()))
                .firstSequence(Optional.of(configuration.getFirstSequence()))
                .build();
    }

}
