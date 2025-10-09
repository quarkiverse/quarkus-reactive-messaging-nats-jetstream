package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Client;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfiguration;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.mutiny.Multi;

public class MessagePullPublisherProcessor<T> extends MessagePublisherProcessor<T> {
    private final ConsumerConfiguration<T> configuration;
    private final PullConfiguration pullConfiguration;
    private final Client client;

    public MessagePullPublisherProcessor(final String channel,
            final String stream,
            final String consumer,
            final Client client,
            final ConsumerConfiguration<T> configuration,
            final PullConfiguration pullConfiguration,
            final Duration retryBackoff) {
        super(channel, stream, consumer, retryBackoff);
        this.configuration = configuration;
        this.pullConfiguration = pullConfiguration;
        this.client = client;
    }

    @Override
    protected Multi<Message<T>> subscribe() {
        return client.subscribe(configuration, pullConfiguration, this);
    }
}
