package io.quarkiverse.reactive.messaging.nats.jetstream.client.stream;

import java.time.Duration;
import java.util.List;

import lombok.Builder;

@Builder
public record SourceInfo(String name, String filterSubject, long lag, Duration active, External external,
        List<SubjectTransform> subjectTransforms, Error error) {
}
