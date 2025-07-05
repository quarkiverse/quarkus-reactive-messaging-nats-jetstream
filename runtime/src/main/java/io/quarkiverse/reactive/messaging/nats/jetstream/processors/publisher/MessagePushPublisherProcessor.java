package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor extends MessagePublisherProcessor {
    private final PushConsumerConfiguration configuration;

    public MessagePushPublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final ConnectionFactory connectionFactory,
            final ConnectionConfiguration connectionConfiguration,
            final PushConsumerConfiguration configuration,
            final Duration retryBackoff) {
        super(channel, stream, consumer, connectionFactory, connectionConfiguration, retryBackoff);
        this.configuration = configuration;
    }

    @Override
    protected Multi<Message<?>> subscription(Connection connection) {
        return connection.subscribe(stream(), consumer(), configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
