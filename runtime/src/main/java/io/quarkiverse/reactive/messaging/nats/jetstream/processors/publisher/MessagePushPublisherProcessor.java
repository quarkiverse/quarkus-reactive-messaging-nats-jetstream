package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ClientFactory;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.Subscription;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConnectionConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final PushConsumerConfiguration configuration;

    public MessagePushPublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final ClientFactory clientFactory,
            final ConnectionConfiguration connectionConfiguration,
            final PushConsumerConfiguration configuration,
            final Duration retryBackoff) {
        super(channel, stream, consumer, clientFactory, connectionConfiguration, retryBackoff);
        this.configuration = configuration;
    }

    @Override
    protected Multi<Message<T>> subscription(Client client) {
        return client.<T> subscribe(stream(), consumer(), configuration)
                .onItem().transformToMulti(Subscription::subscribe);
    }
}
