package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import io.smallrye.mutiny.Multi;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final ConsumerConfiguration configuration;
    private final PushConfiguration pushConfiguration;
    private final Client client;

    public MessagePushPublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final Client client,
            final ConsumerConfiguration configuration,
            final PushConfiguration pushConfiguration,
            final Duration retryBackoff,
            final Class<T> payloadType) {
        super(channel, stream, consumer, retryBackoff, payloadType);
        this.configuration = configuration;
        this.pushConfiguration = pushConfiguration;
        this.client = client;
    }

    @Override
    protected Multi<Message<T>> subscribe(Class<T> payloadType) {
        return client.subscribe(configuration, pushConfiguration, this, payloadType);
    }
}
