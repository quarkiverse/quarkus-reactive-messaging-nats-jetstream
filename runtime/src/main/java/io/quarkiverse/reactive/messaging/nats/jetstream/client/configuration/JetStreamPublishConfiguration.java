package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface JetStreamPublishConfiguration {

    boolean traceEnabled();

    String stream();

    String subject();

}
