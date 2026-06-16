package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

final class PublishTracer implements Tracer {
    private final Instrumenter<Message, Void> instrumenter;

    PublishTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public @NonNull Uni<Message> withTrace(@NonNull Message message) {
        return addTracingMetadata(message)
                .chain(msg -> Uni.createFrom().item(Unchecked.supplier(() -> traceOutgoing(instrumenter, msg))));
    }

    /**
     * For outgoing messages, if the message doesn't already contain a tracing metadata, it attaches one with the current
     * OpenTelemetry context.
     * Reactive messaging outbound connectors, if tracing is supported, will use that context as parent span to trace outbound
     * message transmission.
     */
    private Uni<Message> addTracingMetadata(final Message message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            if (message.getMetadata(TracingMetadata.class).isEmpty()) {
                var otelContext = QuarkusContextStorage.INSTANCE.current();
                return (Message) message.addMetadata(TracingMetadata.withCurrent(otelContext));
            }
            return message;
        }));
    }

    private Instrumenter<Message, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new MessageAttributesExtractor(Operation.PUBLISH);
        InstrumenterBuilder<Message, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor(Operation.PUBLISH));
        return builder.addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(new HeadersTextMapSetter());
    }

    private Message traceOutgoing(Instrumenter<Message, Void> instrumenter, Message message) {
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
