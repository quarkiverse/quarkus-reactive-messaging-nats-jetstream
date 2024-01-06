package io.quarkiverse.reactive.messsaging.nats.jetstream.processors.subscriber;

import java.util.Optional;

import io.quarkiverse.reactive.messsaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

public class DefaultMessageSubscriberConfiguration implements MessageSubscriberConfiguration {
    private final JetStreamConnectorIncomingConfiguration configuration;

    public DefaultMessageSubscriberConfiguration(JetStreamConnectorIncomingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String getChannel() {
        return configuration.getChannel();
    }

    @Override
    public Optional<String> getStream() {
        return configuration.getStream();
    }

    @Override
    public Optional<String> getSubject() {
        return configuration.getSubject();
    }

    @Override
    public boolean traceEnabled() {
        return configuration.getTraceEnabled();
    }
}
