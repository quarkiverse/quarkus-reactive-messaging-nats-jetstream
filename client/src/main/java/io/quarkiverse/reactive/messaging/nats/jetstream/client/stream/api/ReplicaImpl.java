package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record ReplicaImpl(@NonNull String name,
        boolean current,
        boolean offline,
        @NonNull Duration active,
        long lag) implements Replica {
}
