package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PullSubscribeOptions;

public class PullSubscribeOptionsFactory {
    private final JetstreamConsumerConfigurtationFactory consumerConfigurationFactory;

    public PullSubscribeOptionsFactory() {
        this.consumerConfigurationFactory = new JetstreamConsumerConfigurtationFactory();
    }

    public PullSubscribeOptions create(final JetStreamPullConsumerConfiguration configuration) {
        var builder = PullSubscribeOptions.builder();
        configuration.consumerConfiguration().durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.consumerConfiguration().stream());
        builder = builder.configuration(consumerConfigurationFactory.create(configuration.consumerConfiguration()));
        return builder.build();
    }
}
