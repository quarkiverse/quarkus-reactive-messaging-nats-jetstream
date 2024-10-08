package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public class MessagePushPublisherProcessor extends MessagePublisherProcessor {
    private final MessagePushPublisherConfiguration<?> configuration;
    private final ConnectionFactory connectionFactory;
    private final ConnectionConfiguration connectionConfiguration;

    public MessagePushPublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final MessagePushPublisherConfiguration<?> configuration) {
        this.connectionConfiguration = connectionConfiguration;
        this.connectionFactory = connectionFactory;
        this.configuration = configuration;
    }

    @Override
    protected MessagePublisherConfiguration configuration() {
        return configuration;
    }

    @Override
    protected Uni<? extends SubscribeConnection> connect() {
        return connectionFactory.create(connectionConfiguration, this, configuration);
    }
}
