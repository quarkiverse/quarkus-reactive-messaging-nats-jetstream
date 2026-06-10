package io.quarkiverse.reactive.messaging.nats.processors;

public interface MessageProcessor {

    String channel();

    String stream();

    Health health();

}
