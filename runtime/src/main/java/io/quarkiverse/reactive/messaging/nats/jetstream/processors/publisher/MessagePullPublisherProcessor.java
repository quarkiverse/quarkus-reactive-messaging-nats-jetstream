package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.SubscribeConnection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Uni;

public class MessagePullPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final MessagePullPublisherConfiguration<T> configuration;
    private final ConnectionFactory connectionFactory;
    private final ConnectionConfiguration connectionConfiguration;

    public MessagePullPublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final MessagePullPublisherConfiguration<T> configuration) {
        super();
        this.connectionFactory = connectionFactory;
        this.connectionConfiguration = connectionConfiguration;
        this.configuration = configuration;
    }

    @Override
    protected MessagePublisherConfiguration configuration() {
        return configuration;
    }

    @Override
    protected Uni<? extends SubscribeConnection<T>> connect() {
        return connectionFactory.create(connectionConfiguration, this, configuration);
    }
}
