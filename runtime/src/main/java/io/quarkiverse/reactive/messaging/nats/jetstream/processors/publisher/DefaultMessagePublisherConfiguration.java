package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public class DefaultMessagePublisherConfiguration implements MessagePublisherConfiguration {
    private final JetStreamConnectorIncomingConfiguration configuration;

    public DefaultMessagePublisherConfiguration(final JetStreamConnectorIncomingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    @Override
    public String getSubject() {
        return configuration.getSubject()
                .orElseThrow(() -> new RuntimeException(String.format("Subject not configured for channel: %s", getChannel())));
    }

    @Override
    public Optional<String> getDeliverGroup() {
        return configuration.getDeliverGroup();
    }

    @Override
    public Optional<String> getDurable() {
        return configuration.getDurable();
    }

    @Override
    public Long getMaxDeliver() {
        return configuration.getMaxDeliver();
    }

    @Override
    public Optional<String> getBackOff() {
        return configuration.getBackOff();
    }

    @Override
    public boolean traceEnabled() {
        return configuration.getTraceEnabled();
    }

    @Override
    public Optional<String> getType() {
        return configuration.getPayloadType();
    }

    @Override
    public boolean getPull() {
        return configuration.getPull();
    }

    @Override
    public int getPullBatchSize() {
        return configuration.getPullBatchSize();
    }

    @Override
    public int getPullRepullAt() {
        return configuration.getPullRepullAt();
    }

    @Override
    public long getPullPollTimeout() {
        return configuration.getPullPollTimeout();
    }

    @Override
    public Long getRetryBackoff() {
        return configuration.getRetryBackoff();
    }
}
