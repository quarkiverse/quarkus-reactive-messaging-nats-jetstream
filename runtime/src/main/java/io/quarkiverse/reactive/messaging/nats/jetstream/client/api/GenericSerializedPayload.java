package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record GenericSerializedPayload<T>(String id, byte[] data, Class<T> type,
        Map<String, List<String>> headers) implements SerializedPayload<T> {
}
