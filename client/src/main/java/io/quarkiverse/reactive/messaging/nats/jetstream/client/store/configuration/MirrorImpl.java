package io.quarkiverse.reactive.messaging.nats.jetstream.client.store.configuration;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.ConsumerSource;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.External;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.Mirror;
import io.quarkiverse.reactive.messaging.nats.jetstream.client.stream.configuration.SubjectTransform;
import lombok.Builder;

@Builder
record MirrorImpl(@NonNull String sourceName,
        @NonNull String name,
        long startSequence,
        @NonNull Optional<ZonedDateTime> startTime,
        @NonNull Optional<String> filterSubject,
        @NonNull Optional<External> external,
        @NonNull List<SubjectTransform> subjectTransforms,
        @NonNull Optional<ConsumerSource> consumerSource) implements Mirror {
}
