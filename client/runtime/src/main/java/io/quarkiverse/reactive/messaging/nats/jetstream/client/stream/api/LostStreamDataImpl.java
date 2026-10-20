package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record LostStreamDataImpl(@NonNull List<Long> messages, @NonNull Optional<Long> bytes) implements LostStreamData {
}
