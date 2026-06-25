package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

public interface MessageProcessor {

    String channel();

    String stream();

    Health health();

}
