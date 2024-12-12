package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

public interface PullConsumerConfiguration<T> {

    Duration maxExpires();

    Integer batchSize();

    Integer rePullAt();

    Optional<Integer> maxWaiting();

    ConsumerConfiguration<T> consumerConfiguration();
}
