package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PushSubscribeOptions;

public class PushSubscribeOptionsFactory {
    private final ConsumerConfigurtationFactory consumerConfigurtationFactory;

    public PushSubscribeOptionsFactory() {
        this.consumerConfigurtationFactory = new ConsumerConfigurtationFactory();
    }

    public <T> PushSubscribeOptions create(final PushConsumerConfiguration<T> configuration) {
        var builder = PushSubscribeOptions.builder();
        builder = configuration.ordered().map(builder::ordered).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        builder = configuration.consumerConfiguration().durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.consumerConfiguration().stream());
        builder = configuration.deliverSubject().map(builder::deliverSubject).orElse(builder);
        builder = builder.configuration(consumerConfigurtationFactory.create(configuration));
        return builder.build();
    }
}
