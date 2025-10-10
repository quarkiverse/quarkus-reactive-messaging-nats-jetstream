package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

import java.time.Duration;
import java.time.ZonedDateTime;

@Builder
public record Consumer(String stream,
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

}
