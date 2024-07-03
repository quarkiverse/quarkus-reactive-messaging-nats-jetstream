package io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration;

import java.time.Duration;
import java.util.Optional;

import io.nats.client.api.AckPolicy;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;

public class JetstreamConsumerConfigurtationFactory {

    public ConsumerConfiguration create(final JetStreamConsumerConfiguration configuration) {
        var builder = ConsumerConfiguration.builder();
        builder = configuration.durable().map(builder::durable).orElse(builder);
        if (!configuration.filterSubjects().isEmpty()) {
            builder = builder.filterSubjects(configuration.filterSubjects());
        }
        builder = getName(configuration).map(builder::name).orElse(builder);
        builder = builder.ackPolicy(AckPolicy.Explicit);
        builder = configuration.ackWait().map(builder::ackWait).orElse(builder);
        builder = configuration.deliverPolicy().map(builder::deliverPolicy).orElse(builder.deliverPolicy(DeliverPolicy.All));
        builder = configuration.startSeq().map(builder::startSequence).orElse(builder);
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
        if (configuration instanceof JetStreamPullConsumerConfiguration pullConsumerConfiguration) {
            builder = pullConsumerConfiguration.maxWaiting().map(builder::maxPullWaiting).orElse(builder);
            builder = pullConsumerConfiguration.maxRequestExpires().map(builder::maxExpires).orElse(builder);
        }
        if (configuration instanceof JetStreamPushConsumerConfiguration pushConsumerConfiguration) {
            builder = pushConsumerConfiguration.flowControl().map(builder::flowControl).orElse(builder);
            builder = pushConsumerConfiguration.idleHeartbeat().map(builder::idleHeartbeat).orElse(builder);
            builder = pushConsumerConfiguration.rateLimit().map(builder::rateLimit).orElse(builder);
            builder = pushConsumerConfiguration.headersOnly().map(builder::headersOnly).orElse(builder);
        }

        return builder.build();
    }

    public static Optional<String> getName(final JetStreamConsumerConfiguration configuration) {
        if (configuration.durable().isPresent()) {
            return configuration.durable(); // Name must match durable if both are supplied
        } else {
            return configuration.name();
        }
    }
}
