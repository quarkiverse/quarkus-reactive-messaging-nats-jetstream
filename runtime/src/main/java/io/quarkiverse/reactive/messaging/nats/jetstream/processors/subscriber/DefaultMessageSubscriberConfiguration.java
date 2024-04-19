package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;

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
    public String stream() {
        return configuration.getStream().orElseThrow(() -> new IllegalArgumentException("No stream configured"));
    }

    @Override
    public String subject() {
        return configuration.getSubject().orElseThrow((() -> new IllegalArgumentException("No subject configured")));
    }

    @Override
    public boolean traceEnabled() {
        return configuration.getTraceEnabled();
    }

}
