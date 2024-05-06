package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.JetStreamApiException;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionListener;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.Status;
import io.smallrye.mutiny.Multi;

public interface MessagePublisherProcessor extends MessageProcessor, ConnectionListener {
    Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    int CONSUMER_ALREADY_IN_USE = 10013;

    JetStreamClient jetStreamClient();

    MessagePublisherConfiguration configuration();

    Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publish(Connection connection);

    void setStatus(Status status);

    default Multi<? extends Message<?>> getPublisher() {
        return jetStreamClient().getOrEstablishConnection()
                .onItem().transformToMulti(this::publish)
                .onFailure().invoke(throwable -> {
                    if (!isConsumerAlreadyInUse(throwable)) {
                        logger.errorf(throwable, "Failed to publish messages", throwable);
                        setStatus(new Status(false, throwable.getMessage(), ConnectionEvent.CommunicationFailed));
                    }
                })
                .onFailure().retry().withBackOff(configuration().retryBackoff()).indefinitely()
                .onTermination().invoke(this::close)
                .onCancellation().invoke(this::close)
                .onCompletion().invoke(this::close);
    }

    @Override
    default void onEvent(ConnectionEvent event, String messsage) {
        switch (event) {
            case Connected -> setStatus(new Status(true, messsage, event));
            case Closed -> setStatus(new Status(false, messsage, event));
            case Reconnected -> setStatus(new Status(true, messsage, event));
            case DiscoveredServers -> setStatus(new Status(true, messsage, event));
            case Resubscribed -> setStatus(new Status(true, messsage, event));
            case LameDuck -> setStatus(new Status(false, messsage, event));
            case CommunicationFailed -> setStatus(new Status(false, messsage, event));
        }
    }

    private boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException jetStreamApiException) {
            return jetStreamApiException.getApiErrorCode() == CONSUMER_ALREADY_IN_USE;
        }
        return false;
    }
}
