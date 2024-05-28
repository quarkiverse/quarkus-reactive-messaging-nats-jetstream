package io.quarkiverse.reactive.messaging.nats.jetstream.util;

public record PurgeResult(String streamName, boolean success, long purgeCount) {
}
