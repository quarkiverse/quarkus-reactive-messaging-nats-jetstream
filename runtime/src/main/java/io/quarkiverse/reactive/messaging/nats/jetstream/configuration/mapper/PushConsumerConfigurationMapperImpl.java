package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.Stream;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class PushConsumerConfigurationMapperImpl implements PushConsumerConfigurationMapper {
    private final ConsumerConfigurationMapper consumerConfigurationMapper;

    @Override
    public <T> List<PushConsumerConfiguration<T>> map(ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.streams())
                .map(streams -> streams.entrySet().stream()
                        .flatMap(entry -> this.<T> map(entry.getValue().name().orElse(entry.getKey()), entry.getValue())
                                .stream())
                        .toList())
                .orElseGet(List::of);
    }

    @Override
    public <T> List<PushConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.streams())
                .flatMap(streams -> Optional.ofNullable(streams.get(stream)))
                .map(streamConfiguration -> this.<T> map(streamConfiguration.name().orElse(stream), streamConfiguration))
                .orElseGet(List::of);
    }

    private <T> List<PushConsumerConfiguration<T>> map(String stream, Stream configuration) {
        return configuration.pushConsumers().entrySet().stream().map(entry -> PushConsumerConfiguration.<T> builder()
                .consumerConfiguration(
                        consumerConfigurationMapper.map(stream, entry.getKey(), entry.getValue().consumerConfiguration()))
                .pushConfiguration(map(entry.getValue().pushConfiguration())).build()).toList();
    }

    private PushConfiguration map(
            io.quarkiverse.reactive.messaging.nats.jetstream.configuration.PushConfiguration configuration) {
        return PushConfigurationImpl.builder()
                .deliverSubject(configuration.deliverSubject())
                .headersOnly(configuration.headersOnly())
                .deliverGroup(configuration.deliverGroup())
                .flowControl(configuration.flowControl())
                .idleHeartbeat(configuration.idleHeartbeat())
                .rateLimit(configuration.rateLimit())
                .build();
    }
}
