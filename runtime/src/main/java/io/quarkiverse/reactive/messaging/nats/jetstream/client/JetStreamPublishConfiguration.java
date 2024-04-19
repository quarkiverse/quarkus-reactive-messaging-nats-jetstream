package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public interface JetStreamPublishConfiguration {

    boolean traceEnabled();

    String stream();

    String subject();

}
