package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

import java.util.List;

@Builder
public record Placement(String cluster, List<String> tags) {
}
