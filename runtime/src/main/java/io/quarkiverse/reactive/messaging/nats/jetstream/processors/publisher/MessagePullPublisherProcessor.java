package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePullPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final String consumerName;
    private final PullConsumerConfiguration<T> configuration;

    public MessagePullPublisherProcessor(final String channel,
                                         final ConnectionFactory connectionFactory,
                                         final ConnectionConfiguration connectionConfiguration,
                                         final String consumerName,
                                         final PullConsumerConfiguration<T> configuration,
                                         final Duration retryBackoff) {
        super(channel, connectionFactory, connectionConfiguration, retryBackoff);
        this.consumerName = consumerName;
        this.configuration = configuration;
    }

    @Override
    protected Multi<Message<T>> subscription(Connection<T> connection) {
        return connection.subscribe(consumerName, configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
