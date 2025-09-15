package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;

@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final JetStreamConfiguration configuration;
    private final Client client;

    public MessageSubscriberProcessorFactory(JetStreamConfiguration configuration, Client client) {
        this.configuration = configuration;
        this.client = client;
    }

    public <T> MessageSubscriberProcessor<T> create(String channel, String stream, String subject) {
        return new MessageSubscriberProcessor<>(channel, stream, subject, configuration.connection(), clientFactory);
    }

}
