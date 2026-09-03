package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

public record ConsumerConfigurationImpl(@NonNull String name,
        boolean durable,
        @NonNull Optional<String> filterSubject,
        @NonNull Optional<Set<String>> filterSubjects,
        @NonNull Optional<Duration> acknowledgeWait,
        @NonNull Duration acknowledgeTimeout,
        @NonNull DeliverPolicy deliverPolicy,
        long startSequence,
        @NonNull Optional<ZonedDateTime> startTime,
        @NonNull Optional<String> description,
        @NonNull Optional<Duration> inactiveThreshold,
        @NonNull Optional<Long> maxAcknowledgePending,
        @NonNull Optional<Long> maxDeliver,
        @NonNull Optional<List<Duration>> backoff,
        @NonNull ReplayPolicy replayPolicy,
        @NonNull Optional<Integer> replicas,
        boolean memoryStorage,
        @NonNull Optional<String> sampleFrequency,
        @NonNull Map<String, String> metadata,
        boolean headersOnly,
        @NonNull Optional<ZonedDateTime> pauseUntil,
        @NonNull Optional<PullOptions> pullOptions,
        @NonNull Optional<PushOptions> pushOptions) implements ConsumerConfiguration {
}
