package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import java.util.Optional;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public interface MessageSubscriberConfiguration {

    String getChannel();

    Optional<String> getStream();

    Optional<String> getSubject();

    boolean traceEnabled();

    static MessageSubscriberConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessageSubscriberConfiguration(configuration);
    }
}
