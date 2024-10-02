package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

@Builder
public record PurgeResult(String streamName, boolean success, long purgeCount) {
}
