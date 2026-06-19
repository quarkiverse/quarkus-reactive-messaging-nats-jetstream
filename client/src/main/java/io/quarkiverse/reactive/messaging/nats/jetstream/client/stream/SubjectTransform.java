package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import lombok.Builder;

@Builder
public record SubjectTransform(String source, String destination) {
}
