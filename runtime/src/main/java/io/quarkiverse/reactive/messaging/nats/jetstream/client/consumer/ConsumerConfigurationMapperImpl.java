package io.quarkiverse.reactive.messaging.nats.jetstream.client.consumer;

import java.time.Duration;

import jakarta.enterprise.context.ApplicationScoped;

import io.nats.client.api.AckPolicy;

@ApplicationScoped
public class ConsumerConfigurationMapperImpl implements ConsumerConfigurationMapper {

    @Override
    public <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration) {
        return toBuilder(configuration).build();
    }

    @Override
    public <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration,
            final PullConfiguration pullConfiguration) {
        var builder = toBuilder(configuration);
        builder = pullConfiguration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        return builder.build();
    }

    @Override
    public <T> io.nats.client.api.ConsumerConfiguration map(final ConsumerConfiguration<T> configuration,
            final PushConfiguration pushConfiguration) {
        var builder = toBuilder(configuration);
        builder = builder.deliverSubject(pushConfiguration.deliverSubject());
        builder = pushConfiguration.flowControl().map(builder::flowControl).orElse(builder);
        builder = pushConfiguration.idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
        builder = pushConfiguration.rateLimit().map(builder::rateLimit).orElse(builder);
        builder = pushConfiguration.headersOnly().map(builder::headersOnly).orElse(builder);
        builder = pushConfiguration.deliverGroup().map(builder::deliverGroup).orElse(builder);
        return builder.build();
    }

    private <T> io.nats.client.api.ConsumerConfiguration.Builder toBuilder(final ConsumerConfiguration<T> configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(configuration.name());
        }
        builder = builder.filterSubjects(configuration.filterSubjects());
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
        if (!configuration.metadata().isEmpty()) {
            builder = builder.metadata(configuration.metadata());
        }
        builder = configuration.backoff().map(backoff -> backoff.toArray(new Duration[0]))
                .map(builder::backoff).orElse(builder);
        builder = configuration.pauseUntil().map(builder::pauseUntil).orElse(builder);
        return builder;
    }
}
