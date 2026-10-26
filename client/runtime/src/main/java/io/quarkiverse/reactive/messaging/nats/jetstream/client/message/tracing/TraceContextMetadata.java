package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.opentelemetry.context.Context;

/**
 * The OpenTelemetry context of a message. For a message being published, {@link #current()} is the parent of the
 * publish span; for a received message, it is the context of the receive or process span and {@link #previous()} the
 * context it was started in.
 * <p>
 * Only present when tracing is enabled, so it must only be looked up where OpenTelemetry is available.
 *
 * @param current the context of the message; must not be null
 * @param previous the context the span of the message was started in; may be null
 */
public record TraceContextMetadata(@NonNull Context current, @Nullable Context previous) {

    /**
     * @param current the context of the message; must not be null
     * @return the trace metadata of a message with the given context
     */
    public static @NonNull TraceContextMetadata of(@NonNull Context current) {
        return new TraceContextMetadata(current, null);
    }
}
