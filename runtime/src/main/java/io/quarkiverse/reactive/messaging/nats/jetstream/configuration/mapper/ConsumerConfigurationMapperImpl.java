package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationImpl;

@ApplicationScoped
public class ConsumerConfigurationMapperImpl implements ConsumerConfigurationMapper {

    @Override
    public io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConsumerConfiguration configuration) {
        return ConsumerConfigurationImpl.builder()
                .stream(stream)
                .name(configuration.name().orElse(name))
                .durable(configuration.durable())
                .filterSubjects(configuration.filterSubjects().orElseGet(List::of))
                .ackWait(configuration.ackWait())
                .deliverPolicy(configuration.deliverPolicy())
                .startSequence(configuration.startSequence())
                .startTime(configuration.startTime())
                .description(configuration.description())
                .inactiveThreshold(configuration.inactiveThreshold())
                .maxAckPending(configuration.maxAckPending())
                .maxDeliver(configuration.maxDeliver())
                .replayPolicy(configuration.replayPolicy())
                .replicas(configuration.replicas())
                .memoryStorage(configuration.memoryStorage())
                .sampleFrequency(configuration.sampleFrequency())
                .metadata(configuration.metadata())
                .backoff(configuration.backoff())
                .pauseUntil(configuration.pauseUntil())
                .acknowledgeTimeout(configuration.acknowledgeTimeout())
                .build();
    }
}
