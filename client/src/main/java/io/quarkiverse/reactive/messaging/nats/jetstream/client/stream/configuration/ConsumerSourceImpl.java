package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ConsumerSourceImpl(@NonNull String name, @NonNull String deliverSubject) implements ConsumerSource {
}
