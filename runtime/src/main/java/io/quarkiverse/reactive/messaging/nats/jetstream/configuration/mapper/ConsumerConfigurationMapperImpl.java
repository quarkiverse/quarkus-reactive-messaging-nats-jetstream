package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfigurationImpl;

@ApplicationScoped
public class ConsumerConfigurationMapperImpl implements ConsumerConfigurationMapper {

    @Override
    @SuppressWarnings("unchecked")
    public <T> io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration<T> map(String stream,
            String name, io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConsumerConfiguration configuration) {
        return ConsumerConfigurationImpl.<T> builder()
                .stream(stream)
                .name(name)
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
                .payloadType(configuration.payloadType().map(payloadType -> (Class<T>) payloadType))
                .acknowledgeTimeout(configuration.acknowledgeTimeout())
                .build();
    }
}
