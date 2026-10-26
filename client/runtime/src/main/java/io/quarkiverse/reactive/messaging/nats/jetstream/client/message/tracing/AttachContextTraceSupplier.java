package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.context.Context;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.ContextMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;

/**
 * For incoming messages, it fetches OpenTelemetry context from the message and attaches to the duplicated context of the
 * message.
 * Consumer methods will be called in this duplicated context, so the OpenTelemetry context associated with the incoming
 * message will be propagated.
 */
class AttachContextTraceSupplier implements TraceSupplier {

    @SuppressWarnings("resource")
    @Override
    public @NonNull Uni<Message<byte[]>> get(@NonNull Message<byte[]> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            var messageContext = message.getMetadata(ContextMetadata.class)
                    .map(ContextMetadata::context)
                    .orElse(null);
            var otelContext = message.getMetadata(TraceContextMetadata.class)
                    .map(TraceContextMetadata::current)
                    .orElse(Context.current());
            if (messageContext != null && otelContext != null) {
                QuarkusContextStorage.INSTANCE.attach(messageContext, otelContext);
            }
            return message;
        }));
    }
}
