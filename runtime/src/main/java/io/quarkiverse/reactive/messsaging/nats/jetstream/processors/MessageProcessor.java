package io.quarkiverse.reactive.messsaging.nats.jetstream.processors;

public interface MessageProcessor {

    String getChannel();

    Status getStatus();

    void close();

}
