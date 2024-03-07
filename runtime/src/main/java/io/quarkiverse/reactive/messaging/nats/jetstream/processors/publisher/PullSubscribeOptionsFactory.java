package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;

public class PullSubscribeOptionsFactory extends AbstractSubscribeOptionsFactory {

    public PullSubscribeOptions create(final MessagePublisherConfiguration configuration) {
        final var durable = configuration.getDurable().orElse(null);
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return io.nats.client.PullSubscribeOptions.builder()
                .durable(durable)
                .configuration(
                        ConsumerConfiguration.builder()
                                .maxDeliver(maxDeliver)
                                .backoff(getBackOff(backoff).orElse(null))
                                .build())
                .build();
    }

}
