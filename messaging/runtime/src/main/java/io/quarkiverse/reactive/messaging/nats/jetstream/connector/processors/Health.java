package io.quarkiverse.reactive.messaging.nats.jetstream.connector.processors;

import io.nats.client.Connection;
import lombok.Builder;

@Builder
public record Health(boolean healthy, String message) {

    /**
     * Readiness of a channel: not ready unless the connection is established and the channel itself is healthy.
     */
    static Health readiness(String channel, Connection.Status status, Health health) {
        if (status != Connection.Status.CONNECTED) {
            return new Health(false, String.format("NATS connection %s for channel: %s", status, channel));
        }
        return health;
    }

    /**
     * Liveness of a channel: only a closed connection that was not closed on purpose is fatal, because the client
     * never reconnects it. Any other state is recovered by the client or by the retries of the channel.
     */
    static Health liveness(String channel, Connection.Status status, boolean stopped) {
        if (status == Connection.Status.CLOSED && !stopped) {
            return new Health(false, String.format("NATS connection CLOSED for channel: %s", channel));
        }
        return new Health(true, String.format("NATS connection %s for channel: %s", status, channel));
    }
}
