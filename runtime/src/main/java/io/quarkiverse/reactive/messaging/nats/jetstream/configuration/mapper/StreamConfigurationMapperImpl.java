package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;

@ApplicationScoped
public class StreamConfigurationMapperImpl implements StreamConfigurationMapper {

    @Override
    public List<? extends io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration> map(
            ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.streams())
                .map(streams -> map(streams.entrySet()))
                .orElseGet(List::of);
    }

    private List<? extends io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration> map(
            Set<Map.Entry<String, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.Stream>> streams) {
        return streams.stream().flatMap(this::map).toList();
    }

    private Stream<? extends io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration> map(
            Map.Entry<String, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.Stream> entry) {
        return entry.getValue().configuration().stream().map(configuration -> StreamConfigurationImpl.builder()
                .name(entry.getValue().name().orElse(entry.getKey()))
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
                .build());
    }
}
