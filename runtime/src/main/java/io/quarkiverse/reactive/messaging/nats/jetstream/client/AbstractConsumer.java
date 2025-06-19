package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import io.nats.client.api.AckPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;

import java.time.Duration;
import java.util.List;

public abstract class AbstractConsumer {

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name, final ConsumerConfiguration<T> configuration) {
        return builder(name, configuration).build();
    }

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name, final PullConsumerConfiguration<T> configuration) {
        var builder = builder(name, configuration);
        builder = configuration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        return builder.build();
    }

    protected <T> io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name, final PushConsumerConfiguration<T> configuration) {
        var builder = builder(name, configuration);
        builder = configuration.flowControl().map(builder::flowControl).orElse(builder);
        builder = configuration.idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
        builder = configuration.rateLimit().map(builder::rateLimit).orElse(builder);
        builder = configuration.headersOnly().map(builder::headersOnly).orElse(builder);
        builder = configuration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        return builder.build();
    }

    private <T> io.nats.client.api.ConsumerConfiguration.Builder builder(final String name, final ConsumerConfiguration<T> configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        builder = builder.filterSubjects(List.of(configuration.subject()));
        builder = builder.name(name);
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
        builder = configuration.pauseUntil().map(builder::pauseUntil).orElse(builder);
        return builder;
    }

}
