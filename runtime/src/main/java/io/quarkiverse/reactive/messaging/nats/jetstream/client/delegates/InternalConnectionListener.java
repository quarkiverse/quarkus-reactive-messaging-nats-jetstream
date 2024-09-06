package io.quarkiverse.reactive.messaging.nats.jetstream.client.delegates;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.Connection;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.ConnectionEvent;

public class InternalConnectionListener implements io.nats.client.ConnectionListener {
    private final Connection connection;

    public InternalConnectionListener(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public void connectionEvent(io.nats.client.Connection connection, Events type) {
        switch (type) {
            case CONNECTED -> this.connection.fireEvent(ConnectionEvent.Connected, "Connection established");
            case RECONNECTED ->
                this.connection.fireEvent(ConnectionEvent.Reconnected, "Connection reestablished to server");
            case CLOSED -> this.connection.fireEvent(ConnectionEvent.Closed, "Connection closed");
            case DISCONNECTED -> this.connection.fireEvent(ConnectionEvent.Disconnected, "Connection disconnected");
            case RESUBSCRIBED ->
                this.connection.fireEvent(ConnectionEvent.Reconnected, "Connection reestablished to server");
            case LAME_DUCK -> this.connection.fireEvent(ConnectionEvent.CommunicationFailed, "Lame duck mode");
        }
    }
}
