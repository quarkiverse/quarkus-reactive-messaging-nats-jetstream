package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

@Builder
public record External(String api, String deliver) {
}
