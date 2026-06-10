package io.quarkiverse.reactive.nats.jetstream.message.tracing;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.PublishMetadata;
import org.eclipse.microprofile.reactive.messaging.Message;

record MessageSpanNameExtractor<T>(Operation operation) implements SpanNameExtractor<Message<T>> {

    @Override
    public String extract(Message<T> request) {
        String destinationName = getDestination(request);
        return destinationName + " " + operation.toString();
    }

    private String getDestination(Message<T> message) {
        return message.getMetadata(PublishMetadata.class)
                .map(metadata -> String.format("%s.%s", metadata.stream(), metadata.subject()))
                .orElseThrow(() -> new RuntimeException("Publish metadata not found"));
    }
}
