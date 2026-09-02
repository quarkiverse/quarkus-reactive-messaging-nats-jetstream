package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import lombok.Builder;
import lombok.NonNull;

@Builder
public record PurgeResultImpl(@NonNull String stream, boolean success, long purged) implements PurgeResult {
}
