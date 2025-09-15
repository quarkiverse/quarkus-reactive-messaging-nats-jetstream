package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;

public interface ConsumerConfigurationMapper {

    <T> io.nats.client.api.ConsumerConfiguration of(final String name, final ConsumerConfiguration<T> configuration);

    <T> io.nats.client.api.ConsumerConfiguration of(final String name, final PullConsumerConfiguration<T> configuration);

    <T> io.nats.client.api.ConsumerConfiguration of(final String name, final PushConsumerConfiguration<T> configuration);
}
