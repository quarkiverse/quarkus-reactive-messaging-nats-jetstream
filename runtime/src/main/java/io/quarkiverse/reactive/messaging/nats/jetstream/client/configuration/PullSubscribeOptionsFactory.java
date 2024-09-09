package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PullSubscribeOptions;

public class PullSubscribeOptionsFactory {
    private final ConsumerConfigurtationFactory consumerConfigurationFactory;

    public PullSubscribeOptionsFactory() {
        this.consumerConfigurationFactory = new ConsumerConfigurtationFactory();
    }

    public <T> PullSubscribeOptions create(final PullConsumerConfiguration<T> configuration) {
        var builder = PullSubscribeOptions.builder();
        configuration.consumerConfiguration().durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.consumerConfiguration().stream());
        builder = builder.configuration(consumerConfigurationFactory.create(configuration.consumerConfiguration()));
        return builder.build();
    }
}
