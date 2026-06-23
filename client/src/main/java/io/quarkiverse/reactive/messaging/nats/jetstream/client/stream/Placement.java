package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.util.List;

import lombok.Builder;

@Builder
public record Placement(String cluster, List<String> tags) {
}
