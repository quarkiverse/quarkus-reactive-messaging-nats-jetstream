package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public interface MessagePublisherConfiguration {

    String getChannel();

    String getSubject();

    Optional<String> getDeliverGroup();

    Optional<String> getDurable();

    Long getMaxDeliver();

    Optional<String> getBackOff();

    boolean traceEnabled();

    Optional<String> getType();

    boolean getPull();

    int getPullBatchSize();

    int getPullRepullAt();

    long getPullPollTimeout();

    Long getRetryBackoff();

    boolean getExponentialBackoff();

    static MessagePublisherConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePublisherConfiguration(configuration);
    }
}
