package io.quarkiverse.reactive.messaging.nats.client.it;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public record Data(String data, String resourceId) {
}
