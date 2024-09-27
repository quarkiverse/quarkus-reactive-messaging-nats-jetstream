package io.quarkiverse.reactive.messaging.nats.jetstream.client.administration;

import lombok.Builder;

@Builder
public record External(String api,
                       String deliver) {
}
