package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.ConsumerSource;
import lombok.Builder;

@Builder
public record ConsumerSourceImpl(@NonNull String name, @NonNull String deliverSubject) implements ConsumerSource {
}
