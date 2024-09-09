package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

public record PurgeResult(String streamName, boolean success, long purgeCount) {
}
