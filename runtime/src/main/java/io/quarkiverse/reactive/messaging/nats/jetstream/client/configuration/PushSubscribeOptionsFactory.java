package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import io.nats.client.PushSubscribeOptions;

public class PushSubscribeOptionsFactory {
    private final ConsumerConfigurationFactory consumerConfigurationFactory;

    public PushSubscribeOptionsFactory() {
        this.consumerConfigurationFactory = new ConsumerConfigurationFactory();
    }

    public <T> PushSubscribeOptions create(final PushConsumerConfiguration<T> configuration) {
        var builder = PushSubscribeOptions.builder();
        builder = configuration.ordered().map(builder::ordered).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        builder = configuration.consumerConfiguration().durable().map(builder::durable).orElse(builder);
        builder = builder.stream(configuration.consumerConfiguration().stream());
        builder = configuration.deliverSubject().map(builder::deliverSubject).orElse(builder);
        builder = builder.configuration(consumerConfigurationFactory.create(configuration));
        return builder.build();
    }
}
