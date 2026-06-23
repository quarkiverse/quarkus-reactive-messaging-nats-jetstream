package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.Builder;

@Builder
public record StreamInfo(ZonedDateTime createTime,
        StreamConfiguration config,
        StreamState streamState,
        ClusterInfo clusterInfo,
        MirrorInfo mirrorInfo,
        List<SourceInfo> sourceInfos,
        List<StreamAlternate> alternates,
        ZonedDateTime timestamp) {
}
