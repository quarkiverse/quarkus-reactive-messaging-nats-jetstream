package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPushConsumerConfiguration;

public interface MessagePushPublisherConfiguration extends MessagePublisherConfiguration, JetStreamPushConsumerConfiguration {

    static MessagePushPublisherConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePushPublisherConfiguration(configuration);
    }

}
