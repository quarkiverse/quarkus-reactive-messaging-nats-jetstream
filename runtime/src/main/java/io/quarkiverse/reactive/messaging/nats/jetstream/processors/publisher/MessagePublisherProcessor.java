package io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher;

import static io.quarkiverse.reactive.messaging.nats.jetstream.processors.publisher.MessagePullPublisherProcessor.CONSUMER_ALREADY_IN_USE;

import java.time.Duration;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.nats.client.*;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.JetStreamClient;
import io.quarkiverse.reactive.messaging.nats.jetstream.processors.MessageProcessor;
import io.smallrye.mutiny.Multi;

public interface MessagePublisherProcessor extends MessageProcessor {
    Logger logger = Logger.getLogger(MessagePublisherProcessor.class);

    JetStreamClient jetStreamClient();

    MessagePublisherConfiguration configuration();

    Multi<org.eclipse.microprofile.reactive.messaging.Message<?>> publish(Connection connection);

    default Multi<? extends Message<?>> getPublisher() {
        return jetStreamClient().getOrEstablishConnection()
                .onItem().transformToMulti(this::publish)
                .onFailure().invoke(throwable -> {
                    if (!isConsumerAlreadyInUse(throwable)) {
                        logger.errorf(throwable, "Publish failure: %s", throwable.getMessage());
                    }
                    close();
                })
                .onFailure().retry().withBackOff(Duration.ofMillis(configuration().getRetryBackoff())).indefinitely()
                .onTermination().invoke(this::close)
                .onCancellation().invoke(this::close)
                .onCompletion().invoke(this::close);
    }

    private boolean isConsumerAlreadyInUse(Throwable throwable) {
        if (throwable instanceof JetStreamApiException jetStreamApiException) {
            return jetStreamApiException.getApiErrorCode() == CONSUMER_ALREADY_IN_USE;
        }
        return false;
    }
}
