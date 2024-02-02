package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

public interface MessageProcessor {

    String getChannel();

    Status getStatus();

    void close();

}
