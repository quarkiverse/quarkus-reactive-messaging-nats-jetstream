package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public enum ConnectionEvent {
    Connected,
    Closed,
    Disconnected,
    Reconnected,
    Resubscribed,
    DiscoveredServers,
    LameDuck,
    CommunicationFailed;
}
