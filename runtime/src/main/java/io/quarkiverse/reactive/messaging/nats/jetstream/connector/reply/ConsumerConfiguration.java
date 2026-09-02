package io.quarkiverse.reactive.messaging.nats.jetstream.connector.reply;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.DeliverPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.PullOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.PushOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ReplayPolicy;
import lombok.Builder;

@Builder
record ConsumerConfiguration(@NonNull String name,
        @NonNull String replySubject,
        @NonNull String deliverSubject,
        @NonNull Optional<Duration> inactiveThreshold)
        implements
            io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.configuration.ConsumerConfiguration {

    @Override
    public @NonNull String name() {
        return name;
    }

    @Override
    public boolean durable() {
        return false;
    }

    @Override
    public @NonNull Optional<String> filterSubject() {
        return Optional.of(replySubject);
    }

    @Override
    public @NonNull Optional<Set<String>> filterSubjects() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> acknowledgeWait() {
        return Optional.empty();
    }

    @Override
    public @NonNull Duration acknowledgeTimeout() {
        return Duration.ofSeconds(1);
    }

    @Override
    public @NonNull DeliverPolicy deliverPolicy() {
        return DeliverPolicy.New;
    }

    @Override
    public long startSequence() {
        return 0;
    }

    @Override
    public @NonNull Optional<ZonedDateTime> startTime() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<String> description() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Duration> inactiveThreshold() {
        return inactiveThreshold;
    }

    @Override
    public @NonNull Optional<Long> maxAcknowledgePending() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<Long> maxDeliver() {
        return Optional.of(1L);
    }

    @Override
    public @NonNull Optional<List<Duration>> backoff() {
        return Optional.empty();
    }

    @Override
    public @NonNull ReplayPolicy replayPolicy() {
        return ReplayPolicy.Instant;
    }

    @Override
    public @NonNull Optional<Integer> replicas() {
        return Optional.empty();
    }

    @Override
    public boolean memoryStorage() {
        return false;
    }

    @Override
    public @NonNull Optional<String> sampleFrequency() {
        return Optional.empty();
    }

    @Override
    public @NonNull Map<String, String> metadata() {
        return Map.of();
    }

    @Override
    public boolean headersOnly() {
        return false;
    }

    @Override
    public @NonNull Optional<ZonedDateTime> pauseUntil() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PullOptions> pullOptions() {
        return Optional.empty();
    }

    @Override
    public @NonNull Optional<PushOptions> pushOptions() {
        return Optional.of(new PushOptions() {
            @Override
            public @NonNull String deliverSubject() {
                return deliverSubject;
            }

            @Override
            public boolean flowControl() {
                return false;
            }

            @Override
            public @NonNull Optional<Duration> idleHeartbeat() {
                return Optional.empty();
            }

            @Override
            public @NonNull Optional<Long> rateLimit() {
                return Optional.empty();
            }

            @Override
            public @NonNull Optional<String> deliverGroup() {
                return Optional.empty();
            }
        });
    }
}
