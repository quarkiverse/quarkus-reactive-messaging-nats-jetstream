package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;

public class PushSubscribeOptionsFactory extends AbstractSubscribeOptionsFactory {

    public PushSubscribeOptions create(final MessagePublisherConfiguration configuration) {
        final var deliverGroup = configuration.getDeliverGroup().orElse(null);
        final var durable = configuration.getDurable().orElse(null);
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return create(durable, deliverGroup, backoff, maxDeliver);
    }

    PushSubscribeOptions create(final String durable,
            final String deliverGroup,
            String[] backoff,
            Long maxDeliever) {
        return PushSubscribeOptions.builder()
                .deliverGroup(deliverGroup)
                .durable(durable)
                .configuration(
                        ConsumerConfiguration.builder()
                                .maxDeliver(maxDeliever)
                                .backoff(getBackOff(backoff).orElse(null))
                                .build())
                .build();
    }

}
