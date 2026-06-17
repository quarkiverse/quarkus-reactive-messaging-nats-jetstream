package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

@Builder
public record PurgeResult(String stream, boolean success, long purgeCount) {
}
