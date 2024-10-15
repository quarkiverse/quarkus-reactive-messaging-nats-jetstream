package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final MessagePushPublisherConfiguration<T> configuration;

    public MessagePushPublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final MessagePushPublisherConfiguration<T> configuration) {
        super(connectionFactory, connectionConfiguration);
        this.configuration = configuration;
    }

    @Override
    protected MessagePublisherConfiguration configuration() {
        return configuration;
    }

    @Override
    protected Uni<Subscription<T>> subscription(Connection connection) {
        return connection.subscribtion(configuration);
    }
}
