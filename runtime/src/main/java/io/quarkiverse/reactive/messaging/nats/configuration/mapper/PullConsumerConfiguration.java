package io.quarkiverse.reactive.messaging.nats.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.client.consumer.PullConfiguration;
import lombok.Builder;

@Builder
public record PullConsumerConfiguration(ConsumerConfiguration consumerConfiguration,
        PullConfiguration pullConfiguration) {
}
