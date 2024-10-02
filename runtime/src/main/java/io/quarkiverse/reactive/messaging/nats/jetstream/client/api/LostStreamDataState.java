package io.quarkiverse.reactive.messaging.nats.jetstream.client.api;

import java.util.List;

import lombok.Builder;

@Builder
public record LostStreamDataState(List<Long> messages, Long bytes) {
}
