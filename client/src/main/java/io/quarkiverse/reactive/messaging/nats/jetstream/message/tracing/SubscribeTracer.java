package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

final class SubscribeTracer<T> implements Tracer<T> {
    private final Instrumenter<Message<T>, Void> instrumenter;
    private final TraceSupplier<T> traceSupplier;

    SubscribeTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
        this.traceSupplier = new AttachContextTraceSupplier<>();
    }

    @Override
    public @NonNull Uni<Message<T>> withTrace(@NonNull Message<T> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> traceIncoming(instrumenter, message)))
                .chain(traceSupplier::get);
    }

    private Instrumenter<Message<T>, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new MessageAttributesExtractor<T>(Operation.RECEIVE);
        InstrumenterBuilder<Message<T>, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor<>(Operation.RECEIVE));
        return builder.addAttributesExtractor(attributesExtractor)
                .buildConsumerInstrumenter(new MessageHeadersTextMapGetter<>());
    }

    private Message<T> traceIncoming(Instrumenter<Message<T>, Void> instrumenter, Message<T> message) {
        TracingMetadata tracingMetadata = TracingMetadata.fromMessage(message).orElse(TracingMetadata.empty());
        Context parentContext = tracingMetadata.getPreviousContext();
        if (parentContext == null) {
            parentContext = Context.current();
        }
        boolean shouldStart = instrumenter.shouldStart(parentContext, message);

        if (shouldStart) {
            Context spanContext = instrumenter.start(parentContext, message);
            try (Scope ignored = spanContext.makeCurrent()) {
                TracingMetadata newTracingMetadata = TracingMetadata.with(spanContext, parentContext);
                final var msg = message.addMetadata(newTracingMetadata);
                instrumenter.end(spanContext, message, null, null);
                return msg;
            }
        }
        return message;
    }

    private OpenTelemetry getOpenTelemetry(Instance<OpenTelemetry> openTelemetryInstance) {
        if (openTelemetryInstance.isResolvable()) {
            return openTelemetryInstance.get();
        }
        return GlobalOpenTelemetry.get();
    }
}
