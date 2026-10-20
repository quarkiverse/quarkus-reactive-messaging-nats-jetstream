package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;
import lombok.Builder;

@Builder
record ExternalImpl(@NonNull Optional<String> api, @NonNull Optional<String> deliver) implements External {
}
