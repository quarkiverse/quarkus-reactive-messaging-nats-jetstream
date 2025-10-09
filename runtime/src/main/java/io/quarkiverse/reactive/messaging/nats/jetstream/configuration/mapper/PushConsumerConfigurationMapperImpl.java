package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfigurationImpl;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.ConnectorConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.StreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public record PushConsumerConfigurationMapperImpl(ConsumerConfigurationMapper consumerConfigurationMapper) implements PushConsumerConfigurationMapper {

    @Override
    public <T> List<PushConsumerConfiguration<T>> map(ConnectorConfiguration configuration) {
        return configuration.streams().entrySet().stream().flatMap(entry -> this.<T>map(entry.getKey(), entry.getValue()).stream()).toList();
    }

    @Override
    public <T> List<PushConsumerConfiguration<T>> map(String stream, ConnectorConfiguration configuration) {
        return Optional.ofNullable(configuration.streams().get(stream)).map(streamConfiguration -> this.<T>map(stream, streamConfiguration))
                .orElseGet(List::of);
    }

    private <T> List<PushConsumerConfiguration<T>> map(String stream, StreamConfiguration configuration) {
        return configuration.pushConsumers().entrySet().stream().map(entry -> PushConsumerConfiguration.<T>builder()
                .consumerConfiguration(consumerConfigurationMapper.map(stream, entry.getKey(), entry.getValue().consumerConfiguration()))
                .pushConfiguration(map(entry.getValue().pushConfiguration())).build()).toList();
    }

    private PushConfiguration map(io.quarkiverse.reactive.messaging.nats.jetstream.configuration.PushConfiguration configuration) {
        return PushConfigurationImpl.builder()
                .deliverSubject(configuration.deliverSubject())
                .headersOnly(configuration.headersOnly())
                .deliverGroup(configuration.deliverGroup())
                .flowControl(configuration.flowControl())
                .idleHeartbeat(configuration.idleHeartbeat())
                .ordered(configuration.ordered())
                .rateLimit(configuration.rateLimit())
                .build();
    }
}
