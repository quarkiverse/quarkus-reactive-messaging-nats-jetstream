package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.client.consumer.PushConfiguration;
import lombok.Builder;

@Builder
public record PushConsumerConfiguration<T>(ConsumerConfiguration consumerConfiguration,
        PushConfiguration pushConfiguration) {
}
