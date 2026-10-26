package io.quarkiverse.reactive.messaging.nats.jetstream.client.message;

import java.time.Duration;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

public record NotAcknowledgeMetadataImpl(@NonNull Optional<Duration> withDelay) implements NotAcknowledgeMetadata {
}
