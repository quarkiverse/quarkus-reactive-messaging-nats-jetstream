package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessageOperation.PUBLISH;

import java.util.Optional;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingAttributesExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingSpanNameExtractor;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

public class PublishTracer<T> implements Tracer<T> {
    private final boolean enabled;
    private final Instrumenter<PublishMessageMetadata, Void> instrumenter;

    public PublishTracer(boolean enabled, Instance<OpenTelemetry> openTelemetryInstance) {
        this.enabled = enabled;
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public Uni<Message<T>> withTrace(Message<T> message, TraceSupplier<T> traceSupplier) {
        if (enabled) {
            return addTracingMetadata(message)
                    .onItem().transformToUni(msg -> Uni.createFrom().item(Unchecked.supplier(() -> {
                        msg.getMetadata(PublishMessageMetadata.class)
                                .ifPresent(metadata -> traceOutgoing(instrumenter, msg, metadata));
                        return traceSupplier.get(message);
                    })));
        } else {
            return Uni.createFrom().item(message);
        }
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

    public Instrumenter<PublishMessageMetadata, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new PublishMessageAttributesExtractor();
        final var messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<PublishMessageMetadata, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessagingSpanNameExtractor<>(messagingAttributesGetter, PUBLISH));
        return builder.addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, PUBLISH))
                .addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(new PublishMessageTextMapSetter());
    }

    private void traceOutgoing(Instrumenter<PublishMessageMetadata, Void> instrumenter, Message<T> message,
            PublishMessageMetadata metadata) {
        Optional<TracingMetadata> tracingMetadata = TracingMetadata.fromMessage(message);
        Context parentContext = tracingMetadata.map(TracingMetadata::getCurrentContext).orElse(Context.current());
        boolean shouldStart = instrumenter.shouldStart(parentContext, metadata);
        if (shouldStart) {
            Scope scope = null;
            try {
                Context spanContext = instrumenter.start(parentContext, metadata);
                scope = spanContext.makeCurrent();
                instrumenter.end(spanContext, metadata, null, null);
            } finally {
                if (scope != null) {
                    scope.close();
                }
            }
        }
    }
}
