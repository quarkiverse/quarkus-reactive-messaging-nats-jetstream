package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.api;

import java.util.Optional;

import lombok.Builder;
import lombok.NonNull;

@Builder
record StatusImpl(@NonNull Optional<String> message, int code, boolean error) implements Status {
}
