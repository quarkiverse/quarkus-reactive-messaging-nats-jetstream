package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.setup.RequestReplyPullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.util.RequestReplyConfiguration;

public interface JetStreamPullConsumerConfiguration extends JetStreamConsumerConfiguration {

    Optional<Integer> maxWaiting();

    Optional<Duration> maxExpires();

    Duration pollTimeout();

    static <T> JetStreamPullConsumerConfiguration of(RequestReplyConfiguration<T> requestReplyConfiguration) {
        return new RequestReplyPullConsumerConfiguration<>(requestReplyConfiguration);
    }
}
