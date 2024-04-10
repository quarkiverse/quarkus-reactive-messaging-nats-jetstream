package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;

public class PushSubscribeOptionsFactory extends AbstractSubscribeOptionsFactory {

    public PushSubscribeOptions create(final MessagePublisherConfiguration configuration) {
        final var deliverGroup = configuration.getDeliverGroup().orElse(null);
        final var durable = configuration.getDurable().orElse(null);
        final var backoff = getBackOff(configuration).orElse(null);
        final var maxDeliver = configuration.getMaxDeliver();
        return create(durable, deliverGroup, backoff, maxDeliver, configuration.getMaxAckPending().orElse(null));
    }

    PushSubscribeOptions create(final String durable,
            final String deliverGroup,
            String[] backoff,
            Long maxDeliever,
            Integer maxAckPending) {
        return PushSubscribeOptions.builder()
                .deliverGroup(deliverGroup)
                .durable(durable)
                .configuration(consumerConfiguration(backoff, maxDeliever, maxAckPending))
                .build();
    }

    private ConsumerConfiguration consumerConfiguration(String[] backoff, Long maxDeliever, Integer maxAckPending) {
        if (maxAckPending != null) {
            return ConsumerConfiguration.builder()
                    .maxDeliver(maxDeliever)
                    .maxAckPending(maxAckPending)
                    .backoff(getBackOff(backoff).orElse(null))
                    .build();
        } else {
            return ConsumerConfiguration.builder()
                    .maxDeliver(maxDeliever)
                    .backoff(getBackOff(backoff).orElse(null))
                    .build();
        }
    }

}
