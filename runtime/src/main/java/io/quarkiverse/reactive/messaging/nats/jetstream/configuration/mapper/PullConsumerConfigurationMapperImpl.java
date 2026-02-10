package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.StreamConfiguration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class PullConsumerConfigurationMapperImpl implements PullConsumerConfigurationMapper {
    private final ConsumerConfigurationMapper consumerConfigurationMapper;

    @Override
    public List<PullConsumerConfiguration> map(ConnectorConfiguration configuration) {
        if (configuration.streams() == null) {
            return List.of();
        }
        return Optional.ofNullable(configuration.streams())
                .map(streams -> streams.entrySet().stream()
                        .flatMap(entry -> this.map(entry.getValue().name().orElse(entry.getKey()), entry.getValue())
                                .stream())
                        .toList())
                .orElseGet(List::of);
    }

    @Override
    public List<PullConsumerConfiguration> map(String stream, ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.streams())
                .flatMap(streams -> Optional.ofNullable(streams.get(stream)))
                .map(streamConfiguration -> this.map(streamConfiguration.name().orElse(stream), streamConfiguration))
                .orElseGet(List::of);
    }

    private List<PullConsumerConfiguration> map(String stream, StreamConfiguration configuration) {
        return configuration.pullConsumers().entrySet().stream().map(entry -> PullConsumerConfiguration.builder()
                .consumerConfiguration(
                        consumerConfigurationMapper.map(stream, entry.getKey(), entry.getValue().consumerConfiguration()))
                .pullConfiguration(map(entry.getValue().pullConfiguration())).build()).toList();
    }

    private PullConfiguration map(
            io.quarkiverse.reactive.messaging.nats.jetstream.configuration.PullConfiguration configuration) {
        return PullConfigurationImpl.builder()
                .rePullAt(configuration.rePullAt())
                .maxWaiting(configuration.maxWaiting())
                .batchSize(configuration.batchSize())
                .maxExpires(configuration.maxExpires())
                .build();
    }
}
