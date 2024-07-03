package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PushSubscribeOptions;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePushPublisherConfiguration;

public class PushSubscribeOptionsFactory extends JetstreamConsumerConfigurtationFactory {
    private final JetstreamConsumerConfigurtationFactory consumerConfigurtationFactory;

    public PushSubscribeOptionsFactory() {
        this.consumerConfigurtationFactory = new JetstreamConsumerConfigurtationFactory();
    }

    public <T> PushSubscribeOptions create(final MessagePushPublisherConfiguration<T> configuration) {
        var builder = PushSubscribeOptions.builder();
        builder = configuration.ordered().map(builder::ordered).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        builder = configuration.durable().map(builder::durable).orElse(builder);
        builder = builder.configuration(consumerConfigurtationFactory.create(configuration));
        builder = builder.stream(configuration.stream());
        return builder.build();
    }
}
