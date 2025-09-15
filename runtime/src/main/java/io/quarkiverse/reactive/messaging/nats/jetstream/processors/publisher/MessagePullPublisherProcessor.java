package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePullPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final PullConsumerConfiguration configuration;

    public MessagePullPublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final Client client,
            final ConnectionConfiguration connectionConfiguration,
            final PullConsumerConfiguration configuration,
            final Duration retryBackoff) {
        super(channel, stream, consumer, client, connectionConfiguration, retryBackoff);
        this.configuration = configuration;
    }

    @Override
    protected Multi<Message<T>> subscription(Client client) {
        return client.<T> subscribe(stream(), consumer(), configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
