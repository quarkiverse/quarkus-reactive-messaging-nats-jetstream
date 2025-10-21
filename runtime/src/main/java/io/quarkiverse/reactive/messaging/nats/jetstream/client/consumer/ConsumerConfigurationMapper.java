package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

public interface ConsumerConfigurationMapper {

    <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration);

    <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration,
            final PullConfiguration pullConfiguration);

    <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration,
            final PushConfiguration pushConfiguration);
}
