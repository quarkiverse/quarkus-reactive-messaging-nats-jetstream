package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public enum DeliverPolicy {
    All,
    Last,
    New,
    ByStartSequence,
    ByStartTime,
    LastPerSubject;
}
