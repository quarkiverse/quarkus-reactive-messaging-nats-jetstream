package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging;

import java.util.Locale;

public enum MessageOperation {
    PUBLISH,
    RECEIVE;

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
