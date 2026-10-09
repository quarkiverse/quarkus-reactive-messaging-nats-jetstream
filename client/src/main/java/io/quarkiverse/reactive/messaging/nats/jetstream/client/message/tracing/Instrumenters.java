package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jspecify.annotations.NonNull;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessagingAttributesExtractor;
import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessagingSpanNameExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;
import io.opentelemetry.instrumentation.api.instrumenter.SpanLinksExtractor;

final class Instrumenters {
    static final String INSTRUMENTATION_NAME = "io.smallrye.reactive.messaging.jetstream";

    private Instrumenters() {
    }

    /**
     * Creates the instrumenter of an operation. Span kinds follow the messaging semantic conventions: publishing is a
     * {@code PRODUCER}, pulling a message on demand a {@code CLIENT} and processing a delivered message a
     * {@code CONSUMER} span. Received and processed messages link to the span that published them.
     */
    static @NonNull Instrumenter<Message<byte[]>, Void> create(@NonNull OpenTelemetry openTelemetry,
            @NonNull Operation operation,
            boolean tracePayload) {
        final var getter = new MessagingAttributesGetterImpl(operation);
        final var messageOperation = switch (operation) {
            case PUBLISH -> MessageOperation.PUBLISH;
            case RECEIVE -> MessageOperation.RECEIVE;
            case PROCESS -> MessageOperation.PROCESS;
        };
        final var builder = Instrumenter.<Message<byte[]>, Void> builder(openTelemetry, INSTRUMENTATION_NAME,
                MessagingSpanNameExtractor.create(getter, messageOperation))
                .addAttributesExtractor(MessagingAttributesExtractor.create(getter, messageOperation))
                .addAttributesExtractor(new NatsAttributesExtractor(operation, tracePayload));
        return switch (operation) {
            case PUBLISH -> builder.buildProducerInstrumenter(new HeadersTextMapSetter());
            case RECEIVE -> builder.addSpanLinksExtractor(producerLink(openTelemetry, operation))
                    .buildInstrumenter(SpanKindExtractor.alwaysClient());
            case PROCESS -> builder.addSpanLinksExtractor(producerLink(openTelemetry, operation))
                    .buildInstrumenter(SpanKindExtractor.alwaysConsumer());
        };
    }

    /**
     * The context of the span that published the message, extracted from the message headers on top of
     * {@code context}.
     */
    static @NonNull Context producerContext(@NonNull OpenTelemetry openTelemetry, @NonNull Operation operation,
            @NonNull Context context, @NonNull Message<byte[]> message) {
        return openTelemetry.getPropagators().getTextMapPropagator()
                .extract(context, message, new MessageHeadersTextMapGetter(operation));
    }

    private static SpanLinksExtractor<Message<byte[]>> producerLink(OpenTelemetry openTelemetry, Operation operation) {
        return (spanLinks, parentContext, message) -> {
            final var producerSpanContext = Span.fromContext(
                    producerContext(openTelemetry, operation, Context.root(), message)).getSpanContext();
            if (producerSpanContext.isValid()) {
                spanLinks.addLink(producerSpanContext);
            }
        };
    }
}
