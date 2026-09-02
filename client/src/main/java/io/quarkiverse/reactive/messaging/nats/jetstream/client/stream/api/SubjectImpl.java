package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

public record SubjectImpl(String name, long count) implements Subject {
}
