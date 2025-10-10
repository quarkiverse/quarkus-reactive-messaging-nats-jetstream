package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

import java.util.List;

@Builder
public record Cluster(String name, String leader, List<Replica> replicas) {
}
