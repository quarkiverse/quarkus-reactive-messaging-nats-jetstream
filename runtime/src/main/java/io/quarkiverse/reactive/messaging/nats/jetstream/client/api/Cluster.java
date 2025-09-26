package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;

public record Cluster(String name, String leader, List<Replica> replicas) {
}
