package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.time.Duration;

public class MessagePushPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final ConsumerConfiguration<T> configuration;
    private final PushConfiguration pushConfiguration;
    private final Client client;

    public MessagePushPublisherProcessor(final String channel,
                                         final String stream,
                                         final String consumer,
                                         final Client client,
                                         final ConsumerConfiguration<T> configuration,
                                         final PushConfiguration pushConfiguration,
                                         final Duration retryBackoff) {
        super(channel, stream, consumer, retryBackoff);
        this.configuration = configuration;
        this.pushConfiguration = pushConfiguration;
        this.client = client;
    }

    @Override
    protected Multi<Message<T>> subscribe() {
        return client.subscribe(configuration, pushConfiguration, this);
    }
}
