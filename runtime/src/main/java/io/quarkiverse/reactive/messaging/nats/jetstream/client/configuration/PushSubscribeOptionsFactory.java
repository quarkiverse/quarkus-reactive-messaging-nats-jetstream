package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PushSubscribeOptions;

public class PushSubscribeOptionsFactory {
    private final JetstreamConsumerConfigurtationFactory consumerConfigurtationFactory;

    public PushSubscribeOptionsFactory() {
        this.consumerConfigurtationFactory = new JetstreamConsumerConfigurtationFactory();
    }

    public <T> PushSubscribeOptions create(final JetStreamPushConsumerConfiguration configuration) {
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
