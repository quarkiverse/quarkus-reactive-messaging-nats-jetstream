package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;

public class PullSubscribeOptionsFactory extends AbstractSubscribeOptionsFactory {

    public PullSubscribeOptions create(final MessagePublisherConfiguration configuration) {
        final var durable = configuration.getDurable().orElse(null);
        return io.nats.client.PullSubscribeOptions.builder()
                .durable(durable)
                .configuration(consumerConfiguration(configuration))
                .build();
    }

    private ConsumerConfiguration consumerConfiguration(final MessagePublisherConfiguration configuration) {
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return configuration.getMaxAckPending()
                .map(maxAckPending -> ConsumerConfiguration.builder()
                        .maxDeliver(maxDeliver)
                        .backoff(getBackOff(backoff).orElse(null))
                        .maxAckPending(maxAckPending)
                        .build())
                .orElseGet(() -> ConsumerConfiguration.builder()
                        .maxDeliver(maxDeliver)
                        .backoff(getBackOff(backoff).orElse(null))
                        .build());
    }

}
