package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.context.Context;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;

/**
 * For incoming messages, it fetches OpenTelemetry context from the message and attaches to the duplicated context of the
 * message.
 * Consumer methods will be called on this duplicated context, so the OpenTelemetry context associated with the incoming
 * message will be propagated.
 */
public class AttachContextTraceSupplier<T> implements TraceSupplier<T> {

    @Override
    public Message<T> get(Message<T> message) {
        var messageContext = message.getMetadata(LocalContextMetadata.class)
                .map(LocalContextMetadata::context)
                .orElse(null);
        var otelContext = TracingMetadata.fromMessage(message)
                .map(TracingMetadata::getCurrentContext)
                .orElse(Context.current());
        if (messageContext != null && otelContext != null) {
            QuarkusContextStorage.INSTANCE.attach(messageContext, otelContext);
        }
        return message;
    }
}
