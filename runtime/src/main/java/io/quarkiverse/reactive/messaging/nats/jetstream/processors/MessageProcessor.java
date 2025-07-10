package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public interface MessageProcessor extends AutoCloseable {

    String channel();

    String stream();

    Status readiness();

    Status liveness();
}
