package io.quarkiverse.reactive.messaging.nats.client.consumer;

public interface ConsumerConfigurationMapper {

    io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration configuration);

    io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration configuration,
            final PullConfiguration pullConfiguration);

    io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration configuration,
            final PushConfiguration pushConfiguration);
}
