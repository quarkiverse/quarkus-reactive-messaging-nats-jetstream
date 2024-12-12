package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Multi;

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
    protected Multi<Message<T>> subscription(Connection<T> connection) {
        return connection.subscribe(configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }

    @Override
    protected Duration retryBackoff() {
        return configuration.retryBackoff();
    }
}
