package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record ClusterInfo(String name,
        String raftGroup,
        String leader,
        ZonedDateTime leaderSince,
        boolean systemAccount,
        String trafficAccount,
        List<Replica> replicas) {
}
