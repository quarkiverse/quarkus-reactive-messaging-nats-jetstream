package io.quarkiverse.reactive.messaging.nats.jetstream.client.mapper;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PullConsumerConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.PushConsumerConfiguration;
import jakarta.enterprise.context.ApplicationScoped;
import org.jspecify.annotations.NonNull;

import java.time.Duration;

@ApplicationScoped
public class DefaultConsumerConfigurationMapper implements ConsumerConfigurationMapper {

    @Override
    public <T> @NonNull ConsumerConfiguration of(@NonNull String name, @NonNull io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration<T> configuration) {
        return builder(name, configuration).build();
    }

    @Override
    public <T> ConsumerConfiguration of(String name, PullConsumerConfiguration<T> configuration) {
        var builder = builder(name, configuration.consumerConfiguration());
        builder = configuration.pullConfiguration().maxWaiting().map(builder::maxPullWaiting).orElse(builder);
        return builder.build();
    }

    @Override
    public <T> ConsumerConfiguration of(String name, PushConsumerConfiguration<T> configuration) {
        var builder = builder(name, configuration.consumerConfiguration());
        builder = builder.deliverSubject(configuration.pushConfiguration().deliverSubject());
        builder = configuration.pushConfiguration().flowControl().map(builder::flowControl).orElse(builder);
        builder = configuration.pushConfiguration().idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
        builder = configuration.pushConfiguration().rateLimit().map(builder::rateLimit).orElse(builder);
        builder = configuration.pushConfiguration().headersOnly().map(builder::headersOnly).orElse(builder);
        builder = configuration.pushConfiguration().deliverGroup().map(builder::deliverGroup).orElse(builder);
        return builder.build();
    }

    private <T> io.nats.client.api.ConsumerConfiguration.Builder builder(final String name,
                                                                     final io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ConsumerConfiguration<T> configuration) {
        var builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (configuration.durable()) {
            builder = builder.durable(name);
        }
        builder = builder.filterSubjects(configuration.filterSubjects());
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
