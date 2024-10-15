package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public interface MessageProcessor extends AutoCloseable {

    String channel();

    Status readiness();

    Status liveness();
}
