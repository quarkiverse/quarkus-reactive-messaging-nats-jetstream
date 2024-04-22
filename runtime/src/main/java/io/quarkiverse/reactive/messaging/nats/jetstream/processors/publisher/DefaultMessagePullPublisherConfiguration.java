package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public class DefaultMessagePullPublisherConfiguration<T> extends AbstractMessagePublisherConfiguration<T>
        implements MessagePullPublisherConfiguration<T> {

    public DefaultMessagePullPublisherConfiguration(JetStreamConnectorIncomingConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Optional<Integer> maxWaiting() {
        return configuration.getPullMaxWaiting();
    }

    @Override
    public Optional<Duration> maxExpires() {
        return configuration.getPullMaxExpires().map(this::toDuration);
    }

    @Override
    public Integer maxRequestBatch() {
        return configuration.getPullBatchSize();
    }

    @Override
    public Duration pollTimeout() {
        return configuration.getPullPollTimeout().map(this::toDuration).orElseGet(() -> Duration.ofMillis(500));
    }

    @Override
    public Integer rePullAt() {
        return configuration.getPullRepullAt();
    }
}
