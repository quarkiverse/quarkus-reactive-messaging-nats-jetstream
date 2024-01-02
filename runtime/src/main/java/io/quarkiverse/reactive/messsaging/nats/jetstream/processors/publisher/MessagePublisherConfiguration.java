package io.quarkiverse.reactive.messsaging.nats.jetstream.processors.publisher;

import java.util.Optional;

import io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public interface MessagePublisherConfiguration {

    String getChannel();

    String getSubject();

    Optional<String> getDeliverGroup();

    Optional<String> getDurable();

    Long getMaxDeliver();

    Optional<String> getBackOff();

    boolean traceEnabled();

    Optional<String> getType();

    static MessagePublisherConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePublisherConfiguration(configuration);
    }
}
