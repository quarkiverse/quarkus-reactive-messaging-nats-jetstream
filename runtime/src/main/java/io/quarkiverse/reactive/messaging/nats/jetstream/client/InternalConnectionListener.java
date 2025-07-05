package io.quarkiverse.reactive.messaging.nats.jetstream.client;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class InternalConnectionListener implements io.nats.client.ConnectionListener {
    private final Connection connection;

    @Override
    public void connectionEvent(io.nats.client.Connection connection, Events type) {
        if (connection != null) {
            switch (type) {
                case CONNECTED -> fireEvent(ConnectionEvent.Connected, "Connection established");
                case RECONNECTED, RESUBSCRIBED -> fireEvent(ConnectionEvent.Reconnected, "Connection reestablished to server");
                case CLOSED -> fireEvent(ConnectionEvent.Closed, "Connection closed");
                case DISCONNECTED -> fireEvent(ConnectionEvent.Disconnected, "Connection disconnected");
                case LAME_DUCK -> fireEvent(ConnectionEvent.CommunicationFailed, "Lame duck mode");
            }
        }
    }

    private void fireEvent(ConnectionEvent event, String message) {
        connection.listeners().forEach(listener -> listener.onEvent(event, message));
    }
}
