package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.api;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration;
import lombok.Builder;

@Builder
record ConsumerImpl(String stream,
        String name,
        ConsumerConfiguration configuration,
        ZonedDateTime created,
        SequenceImpl delivered,
        SequenceImpl ackFloor,
        long pending,
        long waiting,
        long acknowledgePending,
        long redelivered,
        boolean paused,
        Optional<Duration> pauseRemaining,
        boolean pushBound,
        Optional<ZonedDateTime> timestamp) implements Consumer {

}
