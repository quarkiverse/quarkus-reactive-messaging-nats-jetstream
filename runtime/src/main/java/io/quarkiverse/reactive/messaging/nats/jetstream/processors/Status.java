package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;

public record Status(boolean healthy, String message, ConnectionEvent event) {
}
