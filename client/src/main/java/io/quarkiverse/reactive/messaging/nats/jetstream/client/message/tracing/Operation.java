package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

/**
 * The traced messaging operations, following the OpenTelemetry messaging semantic conventions.
 */
public enum Operation {
    /**
     * Publishing a message to a stream. The span covers the publish until JetStream acknowledged it.
     */
    PUBLISH,
    /**
     * Pulling a message on demand ({@code next}/{@code fetch}). The span ends when the message is received.
     */
    RECEIVE,
    /**
     * Processing a message delivered by a subscription. The span ends when the message is acknowledged, or with an
     * error when it is not acknowledged.
     */
    PROCESS
}
