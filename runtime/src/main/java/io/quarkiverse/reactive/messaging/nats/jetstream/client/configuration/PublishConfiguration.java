package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PublishConfiguration {

    String stream();

    String subject();

}
