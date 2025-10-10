package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.StreamConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.PushConsumerConfiguration;

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
                .subjects(allSubjects(configuration))
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

    private Set<String> allSubjects(
            io.quarkiverse.reactive.messaging.nats.jetstream.configuration.StreamConfiguration streamConfiguration) {
        final var subjects = new HashSet<String>();
        streamConfiguration.subjects()
                .ifPresent(streamSubjects -> streamSubjects.forEach(subject -> subjects.add(escape(subject))));
        streamConfiguration.pullConsumers().values().stream()
                .map(PullConsumerConfiguration::consumerConfiguration)
                .forEach(consumer -> subjects.addAll(consumer.filterSubjects().stream().map(this::escape).toList()));
        streamConfiguration.pushConsumers().values().stream()
                .map(PushConsumerConfiguration::consumerConfiguration)
                .forEach(consumer -> subjects.addAll(consumer.filterSubjects().stream().map(this::escape).toList()));
        return subjects;
    }

    private String escape(String subject) {
        if (subject.endsWith(".>")) {
            return subject.substring(0, subject.length() - 2);
        } else {
            return subject;
        }
    }

}
