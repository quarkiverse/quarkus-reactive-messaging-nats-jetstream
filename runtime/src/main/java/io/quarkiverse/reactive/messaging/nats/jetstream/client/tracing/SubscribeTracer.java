package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing;

import static io.opentelemetry.instrumentation.api.instrumenter.messaging.MessageOperation.RECEIVE;
import static io.smallrye.reactive.messaging.tracing.TracingUtils.getOpenTelemetry;

import jakarta.enterprise.inject.Instance;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.InstrumenterBuilder;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingAttributesGetter;
import io.opentelemetry.instrumentation.api.instrumenter.messaging.MessagingSpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.api.SubscribeMessageMetadata;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.smallrye.reactive.messaging.tracing.TracingUtils;

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
                message.getMetadata(SubscribeMessageMetadata.class)
                        .ifPresent(metadata -> TracingUtils.traceIncoming(instrumenter, message, metadata));
                return traceSupplier.get(message);
            }));
        }
        return Uni.createFrom().item(message);
    }

    private Instrumenter<SubscribeMessageMetadata, Void> instrumenter(Instance<OpenTelemetry> openTelemetryInstance) {
        final var attributesExtractor = new SubscribeMessageAttributesExtractor();
        MessagingAttributesGetter<SubscribeMessageMetadata, Void> messagingAttributesGetter = attributesExtractor
                .getMessagingAttributesGetter();
        InstrumenterBuilder<SubscribeMessageMetadata, Void> builder = Instrumenter.builder(
                getOpenTelemetry(openTelemetryInstance),
                "io.smallrye.reactive.messaging.jetstream",
                MessagingSpanNameExtractor.create(messagingAttributesGetter, RECEIVE));
        return builder.addAttributesExtractor(attributesExtractor)
                .addAttributesExtractor(MessagingAttributesExtractor.create(messagingAttributesGetter, RECEIVE))
                .buildConsumerInstrumenter(new SubscribeMessageTextMapGetter());
    }
}
