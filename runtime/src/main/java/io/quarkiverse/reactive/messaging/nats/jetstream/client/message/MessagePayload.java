package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.util.List;
import java.util.Map;

public record MessagePayload(String messageId,
        String stream,
        String subject,
        byte[] content,
        Map<String, List<String>> headers) {
}
