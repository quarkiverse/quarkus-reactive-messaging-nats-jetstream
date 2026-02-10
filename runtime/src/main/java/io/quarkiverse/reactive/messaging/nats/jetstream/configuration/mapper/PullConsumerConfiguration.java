package io.quarkiverse.reactive.messaging.nats.jetstream.configuration.mapper;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer.PullConfiguration;
import lombok.Builder;

@Builder
public record PullConsumerConfiguration(ConsumerConfiguration consumerConfiguration,
        PullConfiguration pullConfiguration) {
}
