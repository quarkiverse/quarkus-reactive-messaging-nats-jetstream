package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

@Builder
public record ConsumerSource(String name, String deliverSubject) {
}
