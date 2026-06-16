package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

final class ConsumerTracer implements Tracer {
    private final Instrumenter<Message, Void> instrumenter;
    private final TraceSupplier traceSupplier;

    ConsumerTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
        this.traceSupplier = new AttachContextTraceSupplier();
    }

    @Override
    public @NonNull Uni<Message> withTrace(@NonNull Message message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> traceIncoming(instrumenter, message)))
                .chain(traceSupplier::get);
    }

    private Instrumenter<Message, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new MessageAttributesExtractor(Operation.RECEIVE);
        InstrumenterBuilder<Message, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor(Operation.RECEIVE));
        return builder.addAttributesExtractor(attributesExtractor)
                .buildConsumerInstrumenter(new MessageHeadersTextMapGetter());
    }

    private Message traceIncoming(Instrumenter<Message, Void> instrumenter, Message message) {
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
                return (Message) msg;
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
