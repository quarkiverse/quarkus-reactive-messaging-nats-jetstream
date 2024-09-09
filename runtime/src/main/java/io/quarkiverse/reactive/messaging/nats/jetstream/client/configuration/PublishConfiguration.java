package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PublishConfiguration {

    boolean traceEnabled();

    String stream();

    String subject();

}
