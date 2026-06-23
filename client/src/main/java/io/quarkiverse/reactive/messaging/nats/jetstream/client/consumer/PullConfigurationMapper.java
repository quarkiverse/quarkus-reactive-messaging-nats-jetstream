package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

@Mapper
public interface PullConfigurationMapper {

    Optional<PullConfiguration> map(io.nats.client.api.ConsumerConfiguration configuration);

    @ObjectFactory
    default Optional<PullConfiguration> create(io.nats.client.api.ConsumerConfiguration configuration) {
        final var optionalMapper = Mappers.getMapper(OptionalMapper.class);
        return Optional.of(PullConfiguration.builder()
                .maxWaiting(optionalMapper.map(configuration.getMaxPullWaiting()))
                .maxRequestExpires(optionalMapper.map(configuration.getMaxExpires()))
                .maxRequestBatch(optionalMapper.map(configuration.getMaxBatch()))
                .maxRequestMaxBytes(optionalMapper.map(configuration.getMaxBytes()))
                .build());
    }

}
