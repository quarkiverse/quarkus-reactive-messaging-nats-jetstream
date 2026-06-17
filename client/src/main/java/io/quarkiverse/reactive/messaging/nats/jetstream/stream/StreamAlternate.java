package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

@Builder
public record StreamAlternate(String name, String domain, String cluster) {
}
