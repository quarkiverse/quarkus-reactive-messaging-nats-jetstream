package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PullSubscribeOptions;

public class PullSubscribeOptionsFactory extends JetstreamConsumerConfigurtationFactory {
    private final JetstreamConsumerConfigurtationFactory consumerConfigurtationFactory;

    public PullSubscribeOptionsFactory() {
        this.consumerConfigurtationFactory = new JetstreamConsumerConfigurtationFactory();
    }

    public PullSubscribeOptions create(final JetStreamPullConsumerConfiguration configuration) {
        var builder = PullSubscribeOptions.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        builder = builder.configuration(consumerConfigurtationFactory.create(configuration));
        builder = builder.stream(configuration.stream());
        return builder.build();
    }
}
