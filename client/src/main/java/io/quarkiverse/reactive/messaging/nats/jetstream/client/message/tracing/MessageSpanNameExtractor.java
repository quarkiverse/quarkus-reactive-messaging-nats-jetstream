package io.quarkiverse.reactive.messaging.nats.jetstream.client.message.tracing;

import org.jspecify.annotations.NonNull;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.Message;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.MessageHeaders;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.message.PublishHeaders;

record MessageSpanNameExtractor(@NonNull Operation operation) implements SpanNameExtractor<Message> {

    @Override
    public String extract(Message request) {
        String destinationName = getDestination(request);
        return destinationName + " " + operation;
    }

    private String getDestination(Message message) {
        return switch (operation) {
            case PUBLISH, PUBLISH_ACKNOWLEDGED -> getPublishDestination(message);
            case RECEIVE -> getMessageDestination(message);
        };
    }

    private String getPublishDestination(Message message) {
        return message.getMetadata(PublishHeaders.class)
                .map(headers -> String.format("%s.%s", headers.stream().orElse(""), headers.subject().orElse("")))
                .orElseThrow(() -> new IllegalArgumentException("No destination found"));
    }

    private String getMessageDestination(Message message) {
        return message.getMetadata(MessageHeaders.class)
                .map(headers -> String.format("%s.%s", headers.stream().orElse(""), headers.subject().orElse("")))
                .orElseThrow(() -> new IllegalArgumentException("No destination found"));
    }
}
