package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import io.nats.client.api.AckPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;

public abstract class AbstractConsumer {

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(
            final ConsumerConfiguration<T> configuration) {
        return builder(configuration).build();
    }

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(
            final PullConsumerConfiguration<T> configuration) {
        var builder = builder(configuration);
        builder = configuration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        return builder.build();
    }

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(
            final PushConsumerConfiguration<T> configuration) {
        var builder = builder(configuration);
        builder = configuration.flowControl().map(builder::flowControl).orElse(builder);
        builder = configuration.idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
        builder = configuration.rateLimit().map(builder::rateLimit).orElse(builder);
        builder = configuration.headersOnly().map(builder::headersOnly).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        return builder.build();
    }

    private <T> io.nats.client.api.ConsumerConfiguration.Builder builder(final ConsumerConfiguration<T> configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(configuration.name());
        }
        builder = builder.filterSubjects(List.of(configuration.subject()));
        builder = builder.name(configuration.name());
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = configuration.ackWait().map(builder::ackWait).orElse(builder);
        builder = builder.deliverPolicy(configuration.deliverPolicy());
        builder = configuration.startSequence().map(builder::startSequence).orElse(builder);
        builder = configuration.startTime().map(builder::startTime).orElse(builder);
        builder = configuration.description().map(builder::description).orElse(builder);
        builder = configuration.inactiveThreshold().map(builder::inactiveThreshold).orElse(builder);
        builder = configuration.maxAckPending().map(builder::maxAckPending).orElse(builder);
        builder = configuration.maxDeliver().map(builder::maxDeliver).orElse(builder);
        builder = builder.replayPolicy(configuration.replayPolicy());
        builder = builder.numReplicas(configuration.replicas());
        builder = configuration.memoryStorage().map(builder::memStorage).orElse(builder);
        builder = configuration.sampleFrequency().map(builder::sampleFrequency).orElse(builder);
        builder = configuration.metadata().map(builder::metadata).orElse(builder);
        builder = configuration.backoff().map(backoff -> backoff.toArray(new Duration[0]))
                .map(builder::backoff).orElse(builder);
        builder = configuration.pauseUntil().map(builder::pauseUntil).orElse(builder);
        return builder;
    }

}
