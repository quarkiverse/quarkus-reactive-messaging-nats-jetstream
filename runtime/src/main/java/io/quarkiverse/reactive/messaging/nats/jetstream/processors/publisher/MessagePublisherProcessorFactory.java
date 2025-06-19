package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.NatsConfiguration;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessagePublisherProcessorFactory {
    private final NatsConfiguration natsConfiguration;
    private final ConnectionFactory connectionFactory;

    public MessagePublisherProcessorFactory(NatsConfiguration natsConfiguration, ConnectionFactory connectionFactory) {
        this.natsConfiguration = natsConfiguration;
        this.connectionFactory = connectionFactory;
    }

    public <T> MessagePublisherProcessor<T> create(String channel) {

    }



}
