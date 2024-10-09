package io.quarkiverse.reactive.messaging.nats.jetstream.processors;

import java.util.concurrent.atomic.AtomicReference;

import org.jboss.logging.Logger;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.smallrye.mutiny.Uni;

public interface MessageProcessor {
    Logger logger = Logger.getLogger(MessageProcessor.class);

    String channel();

    Status readiness();

    Status liveness();

    AtomicReference<? extends Connection> connection();

    default Uni<Void> close() {
        return Uni.createFrom().item(() -> {
            try {
                final var connection = connection().getAndSet(null);
                if (connection != null) {
                    connection.close();
                }
                return null;
            } catch (Throwable failure) {
                logger.warnf(failure, "Failed to close connection with message: %s", failure.getMessage());
                return null;
            }
        });
    }
}
