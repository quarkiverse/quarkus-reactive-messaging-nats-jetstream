package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

public interface PullConsumerConfiguration<T> {

    Optional<Integer> maxWaiting();

    Optional<Duration> maxRequestExpires();

    ConsumerConfiguration<T> consumerConfiguration();

}
