package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

public interface FetchConsumerConfiguration<T> extends ConsumerConfiguration<T> {

    Optional<Duration> fetchTimeout();

}
