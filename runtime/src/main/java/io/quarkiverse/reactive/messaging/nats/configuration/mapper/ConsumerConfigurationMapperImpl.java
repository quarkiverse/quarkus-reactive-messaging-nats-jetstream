package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerConfigurationImpl;

@ApplicationScoped
public class ConsumerConfigurationMapperImpl implements ConsumerConfigurationMapper {

    @Override
    public ConsumerConfiguration map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.configuration.ConsumerConfiguration configuration) {
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
