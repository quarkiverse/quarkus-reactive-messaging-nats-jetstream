package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final PushConsumerConfiguration<T> configuration;
    private final String consumerName;

    public MessagePushPublisherProcessor(final String channel,
                                         final ConnectionFactory connectionFactory,
                                         final ConnectionConfiguration connectionConfiguration,
                                         final String consumerName,
                                         final PushConsumerConfiguration<T> configuration,
                                         final Duration retryBackoff) {
        super(channel, connectionFactory, connectionConfiguration, retryBackoff);
        this.configuration = configuration;
        this.consumerName = consumerName;
    }

    @Override
    protected Multi<Message<T>> subscription(Connection<T> connection) {
        return connection.subscribe(consumerName, configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
