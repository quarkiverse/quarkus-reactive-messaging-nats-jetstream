package io.quarkiverse.reactive.nats.jetstream.message.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import jakarta.enterprise.inject.Instance;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

final class PublishTracer<T> implements Tracer<T> {
    private final Instrumenter<Message<T>, Void> instrumenter;

    PublishTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public @NonNull Uni<Message<T>> withTrace(@NonNull Message<T> message) {
        return addTracingMetadata(message)
                .chain(msg -> Uni.createFrom().item(Unchecked.supplier(() -> traceOutgoing(instrumenter, msg))));
    }

    /**
     * For outgoing messages, if the message doesn't already contain a tracing metadata, it attaches one with the current
     * OpenTelemetry context.
     * Reactive messaging outbound connectors, if tracing is supported, will use that context as parent span to trace outbound
     * message transmission.
     */
    private Uni<Message<T>> addTracingMetadata(final Message<T> message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            if (message.getMetadata(TracingMetadata.class).isEmpty()) {
                var otelContext = QuarkusContextStorage.INSTANCE.current();
                return message.addMetadata(TracingMetadata.withCurrent(otelContext));
            }
            return message;
        }));
    }

    private Instrumenter<Message<T>, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new MessageAttributesExtractor<T>(Operation.PUBLISH);
        InstrumenterBuilder<Message<T>, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor<>(Operation.PUBLISH));
        return builder.addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(new HeadersTextMapSetter<>());
    }

    private Message<T> traceOutgoing(Instrumenter<Message<T>, Void> instrumenter, Message<T> message) {
        Optional<TracingMetadata> tracingMetadata = TracingMetadata.fromMessage(message);
        Context parentContext = tracingMetadata.map(TracingMetadata::getCurrentContext).orElse(Context.current());
        boolean shouldStart = instrumenter.shouldStart(parentContext, message);
        if (shouldStart) {
            Context spanContext = instrumenter.start(parentContext, message);
            try (Scope ignored = spanContext.makeCurrent()) {
                instrumenter.end(spanContext, message, null, null);
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
