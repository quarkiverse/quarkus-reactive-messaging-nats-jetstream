package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;

import io.nats.client.api.AckPolicy;

public class ConsumerConfigurtationFactory {

    public <T> io.nats.client.api.ConsumerConfiguration create(final ConsumerConfiguration<T> configuration) {
        return builder(configuration).build();
    }

    public <T> io.nats.client.api.ConsumerConfiguration create(final PullConsumerConfiguration<T> configuration) {
        var builder = builder(configuration.consumerConfiguration());
        builder = configuration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        builder = configuration.maxRequestExpires().map(builder::maxExpires).orElse(builder);
        return builder.build();
    }

    public <T> io.nats.client.api.ConsumerConfiguration create(final PushConsumerConfiguration<T> configuration) {
        var builder = builder(configuration.consumerConfiguration());
        builder = configuration.flowControl().map(builder::flowControl).orElse(builder);
        builder = configuration.idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
        builder = configuration.rateLimit().map(builder::rateLimit).orElse(builder);
        builder = configuration.headersOnly().map(builder::headersOnly).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        return builder.build();
    }

    private <T> io.nats.client.api.ConsumerConfiguration.Builder builder(final ConsumerConfiguration<T> configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        if (!configuration.filterSubjects().isEmpty()) {
            builder = builder.filterSubjects(configuration.filterSubjects());
        }
        builder = builder.name(configuration.name());
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = configuration.ackWait().map(builder::ackWait).orElse(builder);
        builder = configuration.deliverPolicy().map(builder::deliverPolicy).orElse(builder);
        builder = configuration.startSequence().map(builder::startSequence).orElse(builder);
        builder = configuration.startTime().map(builder::startTime).orElse(builder);
        builder = configuration.description().map(builder::description).orElse(builder);
        builder = configuration.inactiveThreshold().map(builder::inactiveThreshold).orElse(builder);
        builder = configuration.maxAckPending().map(builder::maxAckPending).orElse(builder);
        builder = configuration.maxDeliver().map(builder::maxDeliver).orElse(builder);
        builder = configuration.replayPolicy().map(builder::replayPolicy).orElse(builder);
        builder = configuration.replicas().map(builder::numReplicas).orElse(builder);
        builder = configuration.memoryStorage().map(builder::memStorage).orElse(builder);
        builder = configuration.sampleFrequency().map(builder::sampleFrequency).orElse(builder);
        if (!configuration.metadata().isEmpty()) {
            builder = builder.metadata(configuration.metadata());
        }
        if (!configuration.backoff().isEmpty()) {
            builder = builder.backoff(configuration.backoff().toArray(new Duration[0]));
        }
        builder = configuration.ackPolicy().map(builder::ackPolicy).orElse(builder);
        builder = configuration.pauseUntil().map(builder::pauseUntil).orElse(builder);
        return builder;
    }
}
