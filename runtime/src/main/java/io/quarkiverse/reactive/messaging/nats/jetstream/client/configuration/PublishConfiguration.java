package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

public interface PublishConfiguration {

    /**
     * The name of the stream
     */
    String stream();

    /**
     * The name of the subject
     */
    String subject();

}
