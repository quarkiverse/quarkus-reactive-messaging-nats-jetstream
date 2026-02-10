package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PushConfiguration;
import lombok.Builder;

@Builder
public record PushConsumerConfiguration<T>(ConsumerConfiguration consumerConfiguration,
        PushConfiguration pushConfiguration) {
}
