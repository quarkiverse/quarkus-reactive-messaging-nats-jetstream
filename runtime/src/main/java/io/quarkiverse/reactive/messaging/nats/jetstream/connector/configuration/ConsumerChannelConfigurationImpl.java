package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ConsumerChannelConfigurationImpl(@NonNull String name,
        @NonNull String stream,
        @NonNull Optional<Duration> retryBackoff,
        @NonNull String datasource,
        @NonNull Optional<String> consumer,
        @NonNull Optional<Class<?>> payloadType,
        @NonNull Integer batchSize,
        @NonNull Duration timeout) implements ConsumerChannelConfiguration {
}
