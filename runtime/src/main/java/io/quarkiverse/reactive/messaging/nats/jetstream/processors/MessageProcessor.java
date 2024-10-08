package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public interface MessageProcessor {

    String channel();

    Status readiness();

    Status liveness();

    void close();
}
