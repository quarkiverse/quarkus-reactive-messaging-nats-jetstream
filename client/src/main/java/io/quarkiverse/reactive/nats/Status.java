package io.quarkiverse.reactive.nats;

public enum Status {

    /**
     * The {@code Client} is not connected.
     */
    DISCONNECTED,
    /**
     * The {@code Client} is currently connected.
     */
    CONNECTED,
    /**
     * The {@code Client} is currently closed.
     */
    CLOSED,
    /**
     * The {@code Client} is currently attempting to reconnect to a server from its server list.
     */
    RECONNECTING,
    /**
     * The {@code Client} is currently connecting to a server for the first
     * time.
     */
    CONNECTING;
}
