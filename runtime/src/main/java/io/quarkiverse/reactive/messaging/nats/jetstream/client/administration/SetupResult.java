package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import io.nats.client.api.StreamInfo;

public record SetupResult(StreamInfo streamInfo) {
}
