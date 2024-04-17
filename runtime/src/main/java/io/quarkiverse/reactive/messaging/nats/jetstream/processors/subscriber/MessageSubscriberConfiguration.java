package io.quarkiverse.reactive.messaging.nats.jetstream.processors.subscriber;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPublishConfiguration;

public interface MessageSubscriberConfiguration extends JetStreamPublishConfiguration {

    String getChannel();

    static MessageSubscriberConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessageSubscriberConfiguration(configuration);
    }
}
