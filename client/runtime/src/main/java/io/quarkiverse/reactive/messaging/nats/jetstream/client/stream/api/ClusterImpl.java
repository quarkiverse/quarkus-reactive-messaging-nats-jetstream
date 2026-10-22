package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import lombok.Builder;

@Builder
public record ClusterImpl(Optional<String> name,
        Optional<String> raftGroup,
        Optional<String> leader,
        Optional<ZonedDateTime> leaderSince,
        boolean systemAccount,
        Optional<String> trafficAccount,
        List<Replica> replicas) implements Cluster {
}
