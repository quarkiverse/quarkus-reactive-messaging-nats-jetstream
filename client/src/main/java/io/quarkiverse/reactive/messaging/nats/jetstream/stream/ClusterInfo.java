package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

import lombok.Builder;

import java.time.ZonedDateTime;
import java.util.List;

@Builder
public record ClusterInfo(String name,
                          String raftGroup,
                          String leader,
                          ZonedDateTime leaderSince,
                          boolean systemAccount,
                          String trafficAccount,
                          List<Replica> replicas) {
}
