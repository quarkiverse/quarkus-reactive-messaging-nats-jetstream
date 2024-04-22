package io.quarkiverse.reactive.messaging.nats.jetstream.setup;

import io.nats.client.api.StreamInfo;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;

public record SetupResult(Connection connection, StreamInfo streamInfo) {
}
