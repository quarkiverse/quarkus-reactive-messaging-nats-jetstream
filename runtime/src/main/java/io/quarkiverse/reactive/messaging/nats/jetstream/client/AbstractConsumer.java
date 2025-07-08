package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import io.nats.client.api.AckPolicy;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;

public abstract class AbstractConsumer {

    protected io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name,
            final ConsumerConfiguration configuration) {
        return builder(name, configuration).build();
    }

    protected io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name,
            final PullConsumerConfiguration configuration) {
        var builder = builder(name, configuration.consumerConfiguration());
        builder = configuration.pullConfiguration().maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        return builder.build();
    }

    protected io.nats.client.api.ConsumerConfiguration createConsumerConfiguration(final String name,
            final PushConsumerConfiguration configuration) {
        var builder = builder(name, configuration.consumerConfiguration());
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::flowControl).map(builder::flowControl)
                .orElse(builder);
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::idleHeartbeat).map(builder::idleHeartbeat)
                .orElse(builder);
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::rateLimit).map(builder::rateLimit)
                .orElse(builder);
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::headersOnly).map(builder::headersOnly)
                .orElse(builder);
        builder = configuration.pushConfiguration().flatMap(PushConfiguration::deliverGroup).map(builder::deliverGroup)
                .orElse(builder);
        return builder.build();
    }

    private io.nats.client.api.ConsumerConfiguration.Builder builder(final String name,
            final ConsumerConfiguration configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(name);
        }
        builder = builder.filterSubjects(List.of(configuration.subject()));
        builder = builder.name(name);
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
