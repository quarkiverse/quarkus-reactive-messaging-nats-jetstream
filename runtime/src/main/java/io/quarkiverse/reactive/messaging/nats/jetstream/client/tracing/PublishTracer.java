package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.PUBLISH;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.getOpenTelemetry;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.PublishMessageMetadata;
import io.quarkus.opentelemetry.runtime.QuarkusContextStorage;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.tracing.TracingUtils;

public class PublishTracer implements Tracer {
    private final boolean enabled;
    private final Instrumenter<PublishMessageMetadata, Void> instrumenter;

    public PublishTracer(boolean enabled, Instance<OpenTelemetry> openTelemetryInstance) {
        this.enabled = enabled;
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public Uni<Message<?>> withTrace(Message<?> message, TraceSupplier traceSupplier) {
        if (enabled) {
            return addTracingMetadata(message)
                    .onItem().transformToUni(msg -> Uni.createFrom().item(Unchecked.supplier(() -> {
                        msg.getMetadata(PublishMessageMetadata.class)
                                .ifPresent(metadata -> TracingUtils.traceOutgoing(instrumenter, msg, metadata));
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
    private Uni<Message<?>> addTracingMetadata(final Message<?> message) {
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
                MessagingSpanNameExtractor.create(messagingAttributesGetter, PUBLISH));
        return builder.addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, PUBLISH))
                .addAttributesExtractor(attributesExtractor)
                .buildProducerInstrumenter(new PublishMessageTextMapSetter());
    }
}
