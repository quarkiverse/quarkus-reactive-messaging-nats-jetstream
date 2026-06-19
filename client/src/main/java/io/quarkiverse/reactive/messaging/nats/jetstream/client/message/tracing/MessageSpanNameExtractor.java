package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Headers;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;

record MessageSpanNameExtractor(Operation operation) implements SpanNameExtractor<Message> {

    @Override
    public String extract(Message request) {
        String destinationName = getDestination(request);
        return destinationName + " " + operation.toString();
    }

    private String getDestination(Message message) {
        return message.getMetadata(Headers.class)
                .map(metadata -> String.format("%s.%s", metadata.stream().orElse(""), metadata.subject().orElse("")))
                .orElseThrow(() -> new RuntimeException("Headers not found"));
    }
}
