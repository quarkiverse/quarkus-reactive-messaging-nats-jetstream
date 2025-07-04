package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.configuration.JetStreamConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessageSubscriberProcessorFactory {
    private final JetStreamConfiguration configuration;
    private final ConnectionFactory connectionFactory;

    public MessageSubscriberProcessorFactory(JetStreamConfiguration configuration, ConnectionFactory connectionFactory) {
        this.configuration = configuration;
        this.connectionFactory = connectionFactory;
    }

    public <T> MessageSubscriberProcessor<T> create(String channel, String stream, String subject) {
        return new MessageSubscriberProcessor<>(channel,stream, subject, configuration.connection(), connectionFactory);
    }

}
