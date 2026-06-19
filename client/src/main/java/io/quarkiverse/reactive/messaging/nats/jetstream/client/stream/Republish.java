package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

@Builder
public record Republish(String source, String destination, boolean headersOnly) {
}
