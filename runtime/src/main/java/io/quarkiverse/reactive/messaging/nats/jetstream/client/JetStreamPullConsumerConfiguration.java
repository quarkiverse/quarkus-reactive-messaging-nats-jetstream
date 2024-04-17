package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.Optional;

public interface JetStreamPullConsumerConfiguration extends JetStreamConsumerConfiguration {

    Optional<Integer> maxWaiting();

    Optional<Duration> maxExpires();

    Integer maxRequestBatch();

    Duration pollTimeout();

    Integer rePullAt();
}
