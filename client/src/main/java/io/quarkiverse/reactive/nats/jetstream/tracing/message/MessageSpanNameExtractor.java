package io.quarkiverse.reactive.nats.jetstream.tracing.message;

import io.opentelemetry.instrumentation.api.incubator.semconv.messaging.MessageOperation;
import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;
import io.quarkiverse.reactive.nats.jetstream.message.Message;

public record MessageSpanNameExtractor(MessageInfo info, MessageOperation operation) implements SpanNameExtractor<Message> {

    @Override
    public String extract(Message request) {
        String destinationName = info.getDestination(request);
        return destinationName + " " + operation.toString();
    }

}
