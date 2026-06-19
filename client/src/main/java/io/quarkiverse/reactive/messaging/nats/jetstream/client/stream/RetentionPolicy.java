package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

public enum RetentionPolicy {
    Limits,
    Interest,
    WorkQueue;
}
