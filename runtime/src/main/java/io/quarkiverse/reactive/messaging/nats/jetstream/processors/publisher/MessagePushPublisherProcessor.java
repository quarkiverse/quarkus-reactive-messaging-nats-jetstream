package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final PushConsumerConfiguration<T> configuration;

    public MessagePushPublisherProcessor(final String channel,
            final String stream,
            final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final PushConsumerConfiguration<T> configuration,
            final Duration retryBackoff) {
        super(channel, stream, connectionFactory, connectionConfiguration, retryBackoff);
        this.configuration = configuration;
    }

    @Override
    protected Multi<Message<T>> subscription(Connection<T> connection) {
        return connection.subscribe(stream(), configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
