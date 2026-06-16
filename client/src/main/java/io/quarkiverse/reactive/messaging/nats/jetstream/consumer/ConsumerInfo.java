package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.mapstruct.factory.Mappers;

import lombok.Builder;

@Builder
public record ConsumerInfo(String stream,
        String name,
        ConsumerConfiguration configuration,
        ZonedDateTime created,
        Sequence delivered,
        Sequence ackFloor,
        Long pending,
        Long waiting,
        Long acknowledgePending,
        Long redelivered,
        Boolean paused,
        Duration pauseRemaining,
        Cluster cluster,
        Boolean pushBound,
        ZonedDateTime timestamp) {

    public static ConsumerInfo of(io.nats.client.api.ConsumerInfo consumerInfo) {
        final var mapper = Mappers.getMapper(ConsumerInfoMapper.class);
        return mapper.to(consumerInfo);
    }

}
