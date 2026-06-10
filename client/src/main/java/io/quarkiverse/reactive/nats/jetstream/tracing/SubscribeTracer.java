package io.quarkiverse.reactive.nats.jetstream.tracing;

import static io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessageOperation.RECEIVE;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.MessageHeadersTextMapGetter;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.MessageSpanNameExtractor;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.SubscribeMessageAttributesExtractor;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.SubscribeMessageInfo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

final class SubscribeTracer implements Tracer {
    private final Instrumenter<Message, Void> instrumenter;
    private final TraceSupplier traceSupplier;

    SubscribeTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
        this.traceSupplier = new AttachContextTraceSupplier();
    }

    @Override
    public @NonNull Uni<Message> withTrace(@NonNull Message message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> traceIncoming(instrumenter, message)))
                .chain(traceSupplier::get);
    }

    private Instrumenter<Message, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new SubscribeMessageAttributesExtractor();
        InstrumenterBuilder<Message, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor(new SubscribeMessageInfo(), RECEIVE));
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
            Scope scope = spanContext.makeCurrent();

            TracingMetadata newTracingMetadata = TracingMetadata.with(spanContext, parentContext);
            message.injectMetadata(newTracingMetadata);

            try {
                instrumenter.end(spanContext, message, null, null);
            } finally {
                if (scope != null) {
                    scope.close();
                }
            }
            return message;
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
