package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessageOperation.RECEIVE;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingAttributesExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingAttributesGetter;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging.MessagingSpanNameExtractor;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.providers.MetadataInjectableMessage;

public class SubscribeTracer<T> implements Tracer<T> {
    private static final Logger log = Logger.getLogger(SubscribeTracer.class);

    private final boolean enabled;
    private final Instrumenter<SubscribeMessageMetadata, Void> instrumenter;

    public SubscribeTracer(boolean enabled, Instance<OpenTelemetry> openTelemetryInstance) {
        this.enabled = enabled;
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public Uni<Message<T>> withTrace(Message<T> message, TraceSupplier<T> traceSupplier) {
        log.debugf("Adding trace on thread: %s", Thread.currentThread().getName());
        if (enabled) {
            return Uni.createFrom().item(Unchecked.supplier(() -> {
                final var msg = message.getMetadata(SubscribeMessageMetadata.class)
                        .map(metadata -> traceIncoming(instrumenter, message, metadata)).orElse(message);
                return traceSupplier.get(msg);
            }));
        }
        return Uni.createFrom().item(message);
    }

    private Instrumenter<SubscribeMessageMetadata, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new SubscribeMessageAttributesExtractor();
        MessagingAttributesGetter<SubscribeMessageMetadata> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<SubscribeMessageMetadata, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessagingSpanNameExtractor<>(messagingAttributesGetter, RECEIVE));
        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(new SubscribeMessageTextMapGetter());
    }

    private Message<T> traceIncoming(Instrumenter<SubscribeMessageMetadata, Void> instrumenter, Message<T> msg,
            SubscribeMessageMetadata trace) {
        TracingMetadata tracingMetadata = TracingMetadata.fromMessage(msg).orElse(TracingMetadata.empty());
        Context parentContext = tracingMetadata.getPreviousContext();
        if (parentContext == null) {
            parentContext = Context.current();
        }
        boolean shouldStart = instrumenter.shouldStart(parentContext, trace);

        if (shouldStart) {
            Context spanContext = instrumenter.start(parentContext, trace);
            Scope scope = spanContext.makeCurrent();

            Message<T> message;
            TracingMetadata newTracingMetadata = TracingMetadata.with(spanContext, parentContext);
            if (msg instanceof MetadataInjectableMessage) {
                ((MetadataInjectableMessage<T>) msg).injectMetadata(newTracingMetadata);
                message = msg;
            } else {
                message = msg.addMetadata(newTracingMetadata);
            }

            try {
                instrumenter.end(spanContext, trace, null, null);
            } finally {
                if (scope != null) {
                    scope.close();
                }
            }
            return message;
        }
        return msg;
    }
}
