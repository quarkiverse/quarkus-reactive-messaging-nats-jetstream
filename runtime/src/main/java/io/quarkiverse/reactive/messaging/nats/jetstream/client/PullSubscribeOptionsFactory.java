package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.nats.client.PullSubscribeOptions;

public class PullSubscribeOptionsFactory extends AbstractSubscribeOptionsFactory {

    public PullSubscribeOptions create(final JetStreamPullConsumerConfiguration configuration) {
        var builder = PullSubscribeOptions.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        builder = builder.configuration(consumerConfiguration(configuration));
        return builder.build();
    }
}
