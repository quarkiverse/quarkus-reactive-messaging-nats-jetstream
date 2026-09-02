package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import jakarta.enterprise.inject.Instance;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.TracingMetadata;

public class PublishAcknowledgedTracer implements Tracer {
    private final Instrumenter<Message, Void> instrumenter;

    PublishAcknowledgedTracer(Instance<OpenTelemetry> openTelemetryInstance) {
        this.instrumenter = instrumenter(openTelemetryInstance);
    }

    @Override
    public @NonNull Uni<Message> withTrace(@NonNull Message message) {
        return Uni.createFrom().item(Unchecked.supplier(() -> trace(instrumenter, message)));
    }

    private Message trace(Instrumenter<Message, Void> instrumenter, Message message) {
        return TracingMetadata.fromMessage(message).map(tracingMetadata -> {
            Context parentContext = tracingMetadata.getCurrentContext();
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
        }).orElse(message);
    }

    private Instrumenter<Message, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new MessageAttributesExtractor(Operation.PUBLISH_ACKNOWLEDGED);
        InstrumenterBuilder<Message, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                new MessageSpanNameExtractor(Operation.PUBLISH_ACKNOWLEDGED));
        return builder.addAttributesExtractor(attributesExtractor)
                .buildConsumerInstrumenter(new MessageHeadersTextMapGetter(Operation.PUBLISH_ACKNOWLEDGED));
    }

    private OpenTelemetry getOpenTelemetry(Instance<OpenTelemetry> openTelemetryInstance) {
        if (openTelemetryInstance.isResolvable()) {
            return openTelemetryInstance.get();
        }
        return GlobalOpenTelemetry.get();
    }
}
