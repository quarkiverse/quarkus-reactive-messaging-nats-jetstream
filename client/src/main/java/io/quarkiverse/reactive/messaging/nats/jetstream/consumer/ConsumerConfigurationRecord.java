package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import lombok.Builder;

@Builder
record ConsumerConfigurationRecord(String name,
        String stream,
        Boolean durable,
        List<String> filterSubjects,
        Optional<Duration> acknowledgeWait,
        DeliverPolicy deliverPolicy,
        Optional<Long> startSequence,
        Optional<ZonedDateTime> startTime,
        Optional<String> description,
        Optional<Duration> inactiveThreshold,
        Optional<Long> maxAckPending,
        Optional<Long> maxDeliver,
        ReplayPolicy replayPolicy,
        Optional<Integer> replicas,
        Optional<Boolean> memoryStorage,
        Optional<String> sampleFrequency,
        Map<String, String> metadata,
        List<Duration> backoff,
        Optional<ZonedDateTime> pauseUntil,
        Duration acknowledgeTimeout,
        Optional<PullConfiguration> pull) implements ConsumerConfiguration {
}
