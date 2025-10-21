package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public interface MessageProcessor {

    String channel();

    String stream();

    Health health();

}
