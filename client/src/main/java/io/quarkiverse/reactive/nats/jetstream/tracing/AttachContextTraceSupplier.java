package io.quarkiverse.reactive.nats.jetstream.tracing;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.jspecify.annotations.NonNull;

import io.opentelemetry.context.Context;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.providers.locals.LocalContextMetadata;

/**
 * For incoming messages, it fetches OpenTelemetry context from the message and attaches to the duplicated context of the
 * message.
 * Consumer methods will be called on this duplicated context, so the OpenTelemetry context associated with the incoming
 * message will be propagated.
 */
public class AttachContextTraceSupplier implements TraceSupplier {

    @SuppressWarnings("resource")
    @Override
    public @NonNull Uni<Message> get(@NonNull Message message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
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
        }));
    }
}
