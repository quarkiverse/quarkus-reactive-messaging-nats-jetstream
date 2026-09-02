package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.api;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.SubjectTransform;
import lombok.Builder;

@Builder
public record MirrorImpl(@NonNull String name,
        @NonNull Optional<String> filterSubject,
        long lag,
        @NonNull Optional<Duration> active,
        @Nullable Optional<External> external,
        @NonNull List<SubjectTransform> subjectTransforms,
        @NonNull Optional<Error> error) implements Mirror {
}
