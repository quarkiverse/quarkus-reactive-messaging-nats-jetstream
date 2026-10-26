package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.SubjectTransform;
import lombok.Builder;

@Builder
record SubjectTransformImpl(@NonNull String source, @NonNull String destination) implements SubjectTransform {
}
