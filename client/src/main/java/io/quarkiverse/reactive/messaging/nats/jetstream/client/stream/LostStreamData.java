package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

import java.util.List;

@Builder
public record LostStreamData(List<Long> messages, Long bytes) {
}
