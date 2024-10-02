package io.quarkiverse.reactive.messaging.nats.jetstream.tracing;

import java.util.List;
import java.util.Map;

import io.quarkiverse.reactive.messaging.nats.jetstream.JetStreamIncomingMessage;
import lombok.Builder;

@Builder
public record JetStreamTrace(String stream, String subject, String messageId, Map<String, List<String>> headers,
        String payload) {

    public static JetStreamTrace trace(JetStreamIncomingMessage<?> message) {
        return JetStreamTrace.builder()
                .stream(message.getStream())
                .subject(message.getSubject())
                .messageId(message.getMessageId())
                .headers(message.getHeaders())
                .payload(new String(message.getData()))
                .build();
    }
}
