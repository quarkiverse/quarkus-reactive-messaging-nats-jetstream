package io.quarkiverse.reactive.messaging.nats.jetstream.client;

public enum ConnectionEvent {
    Disconnected,
    Connected,
    Closed,
    Reconnected,
    CommunicationFailed,
    SubscriptionInactive,
}
