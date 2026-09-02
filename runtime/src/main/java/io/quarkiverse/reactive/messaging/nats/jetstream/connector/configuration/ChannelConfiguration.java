package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface ChannelConfiguration {

    @NonNull
    String name();

    @NonNull
    String stream();

    @NonNull
    Optional<Duration> retryBackoff();

    @NonNull
    String datasource();
}
