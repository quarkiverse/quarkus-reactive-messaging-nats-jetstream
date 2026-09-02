package io.quarkiverse.reactive.messaging.nats.jetstream.connector.configuration;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public interface ConsumerChannelConfiguration extends ChannelConfiguration {

    @NonNull
    String consumer();

    @NonNull
    Optional<Class<?>> payloadType();

    @NonNull
    Integer batchSize();

    @NonNull
    Duration timeout();

}
