package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import io.nats.client.JetStreamApiException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;

public interface MessagePublisherProcessor extends MessageProcessor, ConnectionListener {
    int CONSUMER_ALREADY_IN_USE = 10013;

    Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publisher();

    default boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException jetStreamApiException) {
            return jetStreamApiException.getApiErrorCode() == CONSUMER_ALREADY_IN_USE;
        }
        return false;
    }
}
