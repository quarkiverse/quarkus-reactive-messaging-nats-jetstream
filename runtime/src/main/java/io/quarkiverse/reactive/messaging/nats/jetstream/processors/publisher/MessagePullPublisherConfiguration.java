package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamConnectorIncomingConfiguration;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamReaderConsumerConfiguration;

public interface MessagePullPublisherConfiguration<T>
        extends MessagePublisherConfiguration<T>, JetStreamReaderConsumerConfiguration {

    static <T> MessagePullPublisherConfiguration<T> of(JetStreamConnectorIncomingConfiguration configuration) {
        return new DefaultMessagePullPublisherConfiguration<T>(configuration);
    }

}
