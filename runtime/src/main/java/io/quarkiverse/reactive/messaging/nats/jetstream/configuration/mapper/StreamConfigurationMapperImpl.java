package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

@ApplicationScoped
public class StreamConfigurationMapperImpl implements StreamConfigurationMapper {

    @Override
    public List<StreamConfiguration> map(ConnectorConfiguration configuration) {
        return configuration.streams().entrySet().stream()
                .map(entry -> map(entry.getKey(), entry.getValue())).toList();
    }

    private io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration map(String name,
            io.quarkiverse.reactive.messaging.nats.jetstream.configuration.StreamConfiguration configuration) {
        return StreamConfigurationImpl.builder()
                .name(name)
                .description(configuration.description())
                .subjects(configuration.subjects().orElseGet(Set::of))
                .replicas(configuration.replicas())
                .storageType(configuration.storageType())
                .retentionPolicy(configuration.retentionPolicy())
                .compressionOption(configuration.compressionOption())
                .maximumConsumers(configuration.maximumConsumers())
                .maximumMessages(configuration.maximumMessages())
                .maximumMessagesPerSubject(configuration.maximumMessagesPerSubject())
                .maximumBytes(configuration.maximumBytes())
                .maximumAge(configuration.maximumAge())
                .maximumMessageSize(configuration.maximumMessageSize())
                .templateOwner(configuration.templateOwner())
                .discardPolicy(configuration.discardPolicy())
                .duplicateWindow(configuration.duplicateWindow())
                .allowRollup(configuration.allowRollup())
                .allowDirect(configuration.allowDirect())
                .mirrorDirect(configuration.mirrorDirect())
                .denyDelete(configuration.denyDelete())
                .denyPurge(configuration.denyPurge())
                .discardNewPerSubject(configuration.discardNewPerSubject())
                .firstSequence(configuration.firstSequence())
                .build();
    }
}
