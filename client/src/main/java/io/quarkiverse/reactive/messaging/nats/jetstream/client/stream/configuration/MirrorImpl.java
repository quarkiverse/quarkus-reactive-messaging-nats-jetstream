package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import lombok.Builder;

@Builder
public record MirrorImpl(@NonNull String sourceName,
        @NonNull String name,
        long startSequence,
        @NonNull Optional<ZonedDateTime> startTime,
        @NonNull Optional<String> filterSubject,
        @NonNull Optional<External> external,
        @NonNull List<SubjectTransform> subjectTransforms,
        @NonNull Optional<ConsumerSource> consumerSource) implements Mirror {
}
