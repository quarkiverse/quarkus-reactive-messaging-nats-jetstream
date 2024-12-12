package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PullSubscribeOptions;

public class PullSubscribeOptionsFactory {
    private final ConsumerConfigurationFactory consumerConfigurationFactory;

    public PullSubscribeOptionsFactory() {
        this.consumerConfigurationFactory = new ConsumerConfigurationFactory();
    }

    public <T> PullSubscribeOptions create(final PullConsumerConfiguration<T> configuration) {
        var builder = PullSubscribeOptions.builder();
        builder = configuration.consumerConfiguration().durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.consumerConfiguration().stream());
        builder = builder.configuration(consumerConfigurationFactory.create(configuration));
        return builder.build();
    }

    public <T> PullSubscribeOptions create(final ConsumerConfiguration<T> configuration) {
        var builder = PullSubscribeOptions.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.stream());
        builder = builder.configuration(consumerConfigurationFactory.create(configuration));
        return builder.build();
    }
}
