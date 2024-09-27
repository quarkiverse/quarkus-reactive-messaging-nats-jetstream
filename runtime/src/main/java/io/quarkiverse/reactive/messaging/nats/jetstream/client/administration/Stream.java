package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import io.nats.client.api.*;

import java.time.ZonedDateTime;
import java.util.List;

public record Stream(ZonedDateTime created,
                     StreamConfiguration configuration,
                     StreamState streamState,
                     Cluster cluster,
                     MirrorInfo mirrorInfo,
                     List<SourceInfo> sourceInfos,
                     ZonedDateTime timestamp) {
}
