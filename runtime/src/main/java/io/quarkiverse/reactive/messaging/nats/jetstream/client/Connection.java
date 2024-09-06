package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import java.time.Duration;
import java.util.List;

import io.smallrye.mutiny.Uni;

public interface Connection extends AutoCloseable {

    boolean isConnected();

    Uni<Void> flush(Duration duration);

    List<ConnectionListener> listeners();

    void addListener(ConnectionListener listener);

    default void fireEvent(ConnectionEvent event, String message) {
        listeners().forEach(listener -> listener.onEvent(event, message));
    }
}
