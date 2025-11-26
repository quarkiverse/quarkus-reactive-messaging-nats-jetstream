package io.quarkiverse.reactive.messaging.nats.jetstream.client.tracing.messaging;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;

public class MessagingSpanNameExtractor<T> implements SpanNameExtractor<T> {

    private final MessagingAttributesGetter<T> getter;
    private final MessageOperation operation;

    public MessagingSpanNameExtractor(MessagingAttributesGetter<T> getter, MessageOperation operation) {
        this.getter = getter;
        this.operation = operation;
    }

    @Override
    public String extract(T request) {
        String destinationName = getter.getDestination(request);
        if (destinationName == null) {
            destinationName = "unknown";
        }
        return destinationName + " " + operation.operationName();
    }
}
