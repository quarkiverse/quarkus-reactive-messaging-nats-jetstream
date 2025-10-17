package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record PublishMessageMetadata(String stream,
                                     String subject,
                                     String messageId,
                                     SerializedPayload<?> payload,
                                     Map<String, List<String>> headers,
                                     Long sequence) {
}
