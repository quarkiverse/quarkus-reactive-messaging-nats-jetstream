package io.quarkiverse.reactive.messaging.nats.jetstream.consumer;

public enum DeliverPolicy {
    All, Last, New, ByStartSequence, ByStartTime, LastPerSubject;
}
