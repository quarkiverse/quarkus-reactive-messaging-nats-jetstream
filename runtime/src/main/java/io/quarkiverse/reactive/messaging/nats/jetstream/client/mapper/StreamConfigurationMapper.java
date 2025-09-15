package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StreamConfigurationMapper {

    public io.nats.client.api.StreamConfiguration of(String name,
                                                                            StreamConfiguration streamConfiguration) {
        var builder = io.nats.client.api.StreamConfiguration.builder()
                .name(name)
                .storageType(streamConfiguration.storageType())
                .retentionPolicy(streamConfiguration.retentionPolicy())
                .replicas(streamConfiguration.replicas())
                .subjects(streamConfiguration.allSubjects())
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
}
