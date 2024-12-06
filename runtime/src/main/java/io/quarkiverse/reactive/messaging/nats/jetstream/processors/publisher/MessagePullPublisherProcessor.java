package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.TracerFactory;
import io.smallrye.mutiny.Uni;

public class MessagePullPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final MessagePullPublisherConfiguration<T> configuration;

    public MessagePullPublisherProcessor(final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final MessagePullPublisherConfiguration<T> configuration,
            final TracerFactory tracerFactory,
            final Context context) {
        super(connectionFactory, connectionConfiguration, tracerFactory, context);
        this.configuration = configuration;
    }

    @Override
    protected MessagePublisherConfiguration configuration() {
        return configuration;
    }

    @Override
    protected Uni<Subscription<T>> subscription(Connection connection) {
        return connection.subscription(configuration);
    }
}
