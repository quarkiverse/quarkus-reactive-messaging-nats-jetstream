package io.quarkiverse.reactive.nats.jetstream.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessagingAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.nats.jetstream.message.Message;
import io.quarkiverse.reactive.nats.jetstream.message.SubscribeMetadata;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.MessageHeadersTextMapGetter;
import io.quarkiverse.reactive.nats.jetstream.tracing.message.SubscribeMessageAttributesExtractor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import jakarta.enterprise.inject.Instance;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NonNull;

@JBossLog
class SubscribeTracer implements Tracer {
    private final Instrumenter<SubscribeMetadata, Void> instrumenter;
    private final TraceSupplier traceSupplier;

    SubscribeTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
        this.traceSupplier = new AttachContextTraceSupplier();
    }

    @Override
    public @NonNull Uni<Message> withTrace(@NonNull Message message) {
        log.debugf("Adding trace on thread: %s", Thread.currentThread().getName());
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            final var msg = message.getMetadata(SubscribeMetadata.class)
                    .map(metadata -> traceIncoming(instrumenter, message, metadata)).orElse(message);
            return traceSupplier.get(msg);
        }));
    }

    private Instrumenter<SubscribeMetadata, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new SubscribeMessageAttributesExtractor();
        MessagingAttributesGetter<SubscribeMetadata> messagingAttributesGetter = attributesExtractor.getMessagingAttributesGetter();
        InstrumenterBuilder<SubscribeMessageMetadata, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessagingSpanNameExtractor<>(messagingAttributesGetter, RECEIVE));
        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(new MessageHeadersTextMapGetter());
    }

    private Message traceIncoming(Instrumenter<SubscribeMetadata, Void> instrumenter, Message msg,
                                  SubscribeMetadata subscribeMetadata) {
        TracingMetadata tracingMetadata = TracingMetadata.fromMessage(msg).orElse(TracingMetadata.empty());
        Context parentContext = tracingMetadata.getPreviousContext();
        if (parentContext == null) {
            parentContext = Context.current();
        }
        boolean shouldStart = instrumenter.shouldStart(parentContext, subscribeMetadata);

        if (shouldStart) {
            Context spanContext = instrumenter.start(parentContext, subscribeMetadata);
            Scope scope = spanContext.makeCurrent();

            TracingMetadata newTracingMetadata = TracingMetadata.with(spanContext, parentContext);
            Message message = (Message) msg.addMetadata(newTracingMetadata);

            try {
                instrumenter.end(spanContext, subscribeMetadata, null, null);
            } finally {
                if (scope != null) {
                    scope.close();
                }
            }
            return message;
        }
        return msg;
    }

    private OpenTelemetry getOpenTelemetry(Instance<OpenTelemetry> openTelemetryInstance) {
        if (openTelemetryInstance.isResolvable()) {
            return openTelemetryInstance.get();
        }
        return GlobalOpenTelemetry.get();
    }
}
