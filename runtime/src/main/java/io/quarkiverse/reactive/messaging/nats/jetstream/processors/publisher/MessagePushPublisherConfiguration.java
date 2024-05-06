package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.JetStreamPushConsumerConfiguration;

public interface MessagePushPublisherConfiguration<T>
        extends MessagePublisherConfiguration<T>, JetStreamPushConsumerConfiguration {

    static <T> MessagePushPublisherConfiguration<T> of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePushPublisherConfiguration<T>(configuration);
    }

}
