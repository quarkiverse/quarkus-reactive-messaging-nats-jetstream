package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

/**
 * Traces a message pulled on demand. The span ends as soon as the message is received; the context of the span that
 * published the message is its parent and is also linked.
 */
final class ReceiveTracer implements Tracer {
    private final OpenTelemetry openTelemetry;
    private final Instrumenter<Message<byte[]>, Void> instrumenter;
    private final TraceSupplier traceSupplier;

    ReceiveTracer(OpenTelemetry openTelemetry, boolean tracePayload) {
        this.openTelemetry = openTelemetry;
        this.instrumenter = Instrumenters.create(openTelemetry, Operation.RECEIVE, tracePayload);
        this.traceSupplier = new AttachContextTraceSupplier();
    }

    @Override
    public @NonNull Uni<Message<byte[]>> withTrace(@NonNull Message<byte[]> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> trace(message)))
                .chain(traceSupplier::get);
    }

    private Message<byte[]> trace(Message<byte[]> message) {
        final var parentContext = Instrumenters.producerContext(openTelemetry, Operation.RECEIVE, Context.current(), message);
        if (!instrumenter.shouldStart(parentContext, message)) {
            return message;
        }
        final var spanContext = instrumenter.start(parentContext, message);
        instrumenter.end(spanContext, message, null, null);
        return message.addMetadata(TracingMetadata.with(spanContext, parentContext));
    }
}
