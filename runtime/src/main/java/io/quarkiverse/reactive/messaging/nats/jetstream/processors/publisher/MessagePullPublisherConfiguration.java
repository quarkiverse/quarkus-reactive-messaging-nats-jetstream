package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamPullConsumerConfiguration;

public interface MessagePullPublisherConfiguration extends MessagePublisherConfiguration, JetStreamPullConsumerConfiguration {

    static MessagePullPublisherConfiguration of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePullPublisherConfiguration(configuration);
    }

}
