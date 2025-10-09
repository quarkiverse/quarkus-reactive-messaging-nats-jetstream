package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;
import java.util.Map;

import lombok.Builder;

@Builder
public record GenericPayload<P, T>(String id, P data, Class<T> type,
        Map<String, List<String>> headers) implements Payload<P, T> {
}
