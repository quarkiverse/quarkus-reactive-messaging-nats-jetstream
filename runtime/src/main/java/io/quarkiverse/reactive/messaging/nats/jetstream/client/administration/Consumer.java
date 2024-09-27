package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import io.nats.client.api.ClusterInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import lombok.Builder;

import java.time.Duration;
import java.time.ZonedDateTime;

@Builder
public record Consumer(String stream,
                       String name,
                       ConsumerConfiguration<?> configuration,
                       ZonedDateTime created,
                       Sequence delivered,
                       Sequence ackFloor,
                       long pending,
                       long waiting,
                       long acknowledgePending,
                       long numRedelivered,
                       boolean paused,
                       Duration pauseRemaining,
                       ClusterInfo clusterInfo,
                       boolean pushBound,
                       ZonedDateTime timestamp) {
}
