package io.quarkiverse.reactive.messaging.nats.jetstream.message.tracing;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.message.PublishMetadata;

record MessageSpanNameExtractor(Operation operation) implements SpanNameExtractor<Message> {

    @Override
    public String extract(Message request) {
        String destinationName = getDestination(request);
        return destinationName + " " + operation.toString();
    }

    private String getDestination(Message message) {
        return message.getMetadata(PublishMetadata.class)
                .map(metadata -> String.format("%s.%s", metadata.stream(), metadata.subject()))
                .orElseThrow(() -> new RuntimeException("Publish metadata not found"));
    }
}
