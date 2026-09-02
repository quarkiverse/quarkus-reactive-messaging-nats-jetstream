package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

public enum Operation {
    PUBLISH,
    PUBLISH_ACKNOWLEDGED,
    RECEIVE;
}
