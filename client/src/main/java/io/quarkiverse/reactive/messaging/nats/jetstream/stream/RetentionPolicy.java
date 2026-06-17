package io.quarkiverse.reactive.messaging.nats.jetstream.stream;

public enum RetentionPolicy {
    Limits,
    Interest,
    WorkQueue;
}
