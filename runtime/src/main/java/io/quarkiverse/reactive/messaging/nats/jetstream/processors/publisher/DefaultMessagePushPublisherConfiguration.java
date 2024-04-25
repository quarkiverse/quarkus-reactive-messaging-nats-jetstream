package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.time.Duration;
import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public class DefaultMessagePushPublisherConfiguration<T> extends AbstractMessagePublisherConfiguration<T>
        implements MessagePushPublisherConfiguration<T> {

    public DefaultMessagePushPublisherConfiguration(JetStreamConnectorIncomingConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Optional<Boolean> ordered() {
        return configuration.getPushOrdered();
    }

    @Override
    public Optional<String> deliverGroup() {
        return configuration.getPushDeliverGroup();
    }

    @Override
    public Optional<Duration> flowControl() {
        return configuration.getPushFlowControl().map(this::toDuration);
    }

    @Override
    public Optional<Duration> idleHeartbeat() {
        return configuration.getPushIdleHeartBeat().map(this::toDuration);
    }

    @Override
    public Optional<Long> rateLimit() {
        return configuration.getPushRateLimit();
    }

    @Override
    public Optional<Boolean> headersOnly() {
        return Optional.empty();
    }
}
