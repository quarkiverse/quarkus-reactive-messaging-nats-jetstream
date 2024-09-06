package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.configuration.ReaderConsumerConfiguration;

public interface MessagePullPublisherConfiguration<T>
        extends MessagePublisherConfiguration, ReaderConsumerConfiguration<T> {

    static <T> MessagePullPublisherConfiguration<T> of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePullPublisherConfiguration<>(configuration);
    }

}
